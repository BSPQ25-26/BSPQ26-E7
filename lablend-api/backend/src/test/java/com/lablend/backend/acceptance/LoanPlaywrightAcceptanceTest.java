package com.lablend.backend.acceptance;

import com.lablend.backend.auth.service.JwtService;
import com.lablend.backend.entity.Equipment;
import com.lablend.backend.entity.EquipmentStatus;
import com.lablend.backend.entity.Loan;
import com.lablend.backend.entity.LoanStatus;
import com.lablend.backend.entity.User;
import com.lablend.backend.entity.UserRole;
import com.lablend.backend.entity.UserStatus;
import com.lablend.backend.repository.EquipmentRepository;
import com.lablend.backend.repository.LoanRepository;
import com.lablend.backend.repository.UserRepository;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.util.Map;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:lablend_acceptance_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.hibernate.ddl-auto=create-drop"
        }
)
class LoanPlaywrightAcceptanceTest {

    private static final Logger logger = LoggerFactory.getLogger(LoanPlaywrightAcceptanceTest.class);

    @LocalServerPort
    private int port;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private LoanRepository loanRepository;

    private User activeUser;
    private User blockedUser;
    private User maxLoansUser;
    private Equipment availableEquipment;
    private Equipment blockedUserEquipment;
    private Equipment maxLoansEquipment;
    private Playwright playwright;
    private Browser browser;
    private BrowserContext browserContext;
    private Page page;

    @BeforeEach
    void setUp() {
        logger.info("Preparing browser-driven loan acceptance test data.");
        loanRepository.deleteAll();
        equipmentRepository.deleteAll();
        userRepository.deleteAll();

        User admin = userRepository.save(new User(
                "acceptance_admin",
                "acceptance-admin@lablend.local",
                passwordEncoder.encode("password"),
                UserRole.ADMIN));

        activeUser = userRepository.save(new User(
                "acceptance_student",
                "acceptance-student@lablend.local",
                "password",
                UserRole.USER));

        blockedUser = userRepository.save(new User(
                "blocked_student",
                "blocked-student@lablend.local",
                "password",
                UserRole.USER,
                UserStatus.BLOCKED));

        maxLoansUser = userRepository.save(new User(
                "max_loans_student",
                "max-loans-student@lablend.local",
                "password",
                UserRole.USER));

        availableEquipment = equipmentRepository.save(new Equipment(
                "Oscilloscope",
                "Electronics",
                EquipmentStatus.AVAILABLE));

        blockedUserEquipment = equipmentRepository.save(new Equipment(
                "Microscope",
                "Optics",
                EquipmentStatus.AVAILABLE));

        maxLoansEquipment = equipmentRepository.save(new Equipment(
                "Spectrometer",
                "Chemistry",
                EquipmentStatus.AVAILABLE));

        String token = jwtService.generateToken(
                toUserDetails(admin),
                Map.of("role", admin.getRole().name(), "userId", admin.getId()));

        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        browserContext = browser.newContext(new Browser.NewContextOptions()
                .setExtraHTTPHeaders(Map.of("Authorization", "Bearer " + token)));
        page = browserContext.newPage();
    }

    @AfterEach
    void tearDown() {
        logger.info("Cleaning up Playwright acceptance test resources.");
        if (browserContext != null) {
            browserContext.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }

        loanRepository.deleteAll();
        equipmentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void userCanBorrowAvailableEquipmentFromBrowser() {
        page.navigate(acceptancePageUrl());

        page.locator("#userId").fill(activeUser.getId().toString());
        page.locator("#equipmentId").fill(availableEquipment.getId().toString());
        page.locator("#createLoanBtn").click();

        assertThat(page.locator("#message")).containsText("Loan created #");
        assertThat(page.locator("#loanDetails")).containsText("User " + activeUser.getId());
        assertThat(page.locator("#equipmentStatus")).hasText("Equipment status: RESERVED");

        Equipment updatedEquipment = equipmentRepository.findById(availableEquipment.getId()).orElseThrow();
        assertEquals(EquipmentStatus.RESERVED, updatedEquipment.getStatus());
        assertEquals(1, loanRepository.countByUserIdAndStatus(activeUser.getId(), LoanStatus.ACTIVE));
    }

    @Test
    void blockedUserCannotBorrowEquipmentFromBrowser() {
        page.navigate(acceptancePageUrl());

        page.locator("#userId").fill(blockedUser.getId().toString());
        page.locator("#equipmentId").fill(blockedUserEquipment.getId().toString());
        page.locator("#createLoanBtn").click();

        Locator message = page.locator("#message");
        assertThat(message).containsText("User is blocked and cannot borrow equipment");
        assertThat(page.locator("#equipmentStatus")).hasText("Equipment status: AVAILABLE");

        Equipment unchangedEquipment = equipmentRepository.findById(blockedUserEquipment.getId()).orElseThrow();
        assertEquals(EquipmentStatus.AVAILABLE, unchangedEquipment.getStatus());
        assertEquals(0, loanRepository.countByUserIdAndStatus(blockedUser.getId(), LoanStatus.ACTIVE));
    }

    @Test
    void userWithThreeActiveLoansCannotBorrowAnotherEquipmentFromBrowser() {
        seedActiveLoans(maxLoansUser, 3);

        page.navigate(acceptancePageUrl());

        page.locator("#userId").fill(maxLoansUser.getId().toString());
        page.locator("#equipmentId").fill(maxLoansEquipment.getId().toString());
        page.locator("#createLoanBtn").click();

        assertThat(page.locator("#message"))
                .containsText("User has reached the maximum limit of 3 active loans");
        assertThat(page.locator("#equipmentStatus")).hasText("Equipment status: AVAILABLE");

        Equipment unchangedEquipment = equipmentRepository.findById(maxLoansEquipment.getId()).orElseThrow();
        assertEquals(EquipmentStatus.AVAILABLE, unchangedEquipment.getStatus());
        assertEquals(3, loanRepository.countByUserIdAndStatus(maxLoansUser.getId(), LoanStatus.ACTIVE));
    }

    private String acceptancePageUrl() {
        return "http://localhost:" + port + "/acceptance/loan.html";
    }

    private UserDetails toUserDetails(User user) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getName())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    private void seedActiveLoans(User user, int count) {
        for (int index = 1; index <= count; index++) {
            Equipment reservedEquipment = equipmentRepository.save(new Equipment(
                    "Reserved item " + index,
                    "Acceptance",
                    EquipmentStatus.RESERVED));

            Loan loan = new Loan();
            loan.setUserId(user.getId());
            loan.setEquipmentId(reservedEquipment.getId());
            loan.setLoanDate(LocalDateTime.now().minusDays(index));
            loan.setStatus(LoanStatus.ACTIVE);
            loanRepository.save(loan);
        }
    }
}
