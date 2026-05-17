# Services Layer

The service layer implements business logic and coordinates database operations.

## EquipmentService

Manages equipment operations with availability checks and status transitions.

```java
@Service
public class EquipmentServiceImpl implements EquipmentService {
    @Autowired
    private EquipmentRepository equipmentRepository;
    
    public Equipment createEquipment(Equipment equipment) {
        equipment.setStatus(EquipmentStatus.AVAILABLE);
        return equipmentRepository.save(equipment);
    }
    
    public List<Equipment> findAvailable() {
        return equipmentRepository.findByStatus(EquipmentStatus.AVAILABLE);
    }
    
    public Equipment updateStatus(Long id, EquipmentStatus status) {
        Equipment equipment = equipmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));
        equipment.setStatus(status);
        return equipmentRepository.save(equipment);
    }
}
```

**Key Methods**:
- `createEquipment()` — Create new equipment
- `findAvailable()` — Get available equipment
- `updateStatus()` — Change equipment status
- `deleteEquipment()` — Remove equipment

## LoanService

Manages borrowing operations with business rule validation.

```java
@Service
public class LoanServiceImpl implements LoanService {
    @Autowired
    private LoanRepository loanRepository;
    @Autowired
    private EquipmentRepository equipmentRepository;
    
    @Transactional
    public Loan createLoan(Long userId, Long equipmentId, LocalDate returnDate) {
        // Validate equipment exists and is available
        Equipment equipment = equipmentRepository.findById(equipmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));
        
        if (!equipment.getStatus().equals(EquipmentStatus.AVAILABLE)) {
            throw new BadRequestException("Equipment is not available");
        }
        
        // Check borrowing limit (max 3 items)
        long activeLoans = loanRepository.countByUserIdAndStatus(userId, LoanStatus.ACTIVE);
        if (activeLoans >= 3) {
            throw new BadRequestException("Borrowing limit exceeded");
        }
        
        // Check user has no overdue loans
        List<Loan> overdueLoans = loanRepository.findOverdueLoansByUserId(userId);
        if (!overdueLoans.isEmpty()) {
            throw new BadRequestException("User has overdue loans");
        }
        
        // Create and save loan
        Loan loan = new Loan();
        loan.setUserId(userId);
        loan.setEquipmentId(equipmentId);
        loan.setLoanDate(LocalDate.now());
        loan.setPlannedReturnDate(returnDate);
        loan.setStatus(LoanStatus.ACTIVE);
        
        // Update equipment status
        equipment.setStatus(EquipmentStatus.IN_USE);
        equipmentRepository.save(equipment);
        
        return loanRepository.save(loan);
    }
    
    @Transactional
    public Loan returnEquipment(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
        
        if (loan.getStatus().equals(LoanStatus.RETURNED)) {
            throw new BadRequestException("Loan is already returned");
        }
        
        loan.setActualReturnDate(LocalDate.now());
        loan.setStatus(LoanStatus.RETURNED);
        
        // Update equipment status
        Equipment equipment = equipmentRepository.findById(loan.getEquipmentId()).get();
        equipment.setStatus(EquipmentStatus.AVAILABLE);
        equipmentRepository.save(equipment);
        
        return loanRepository.save(loan);
    }
}
```

**Key Methods**:
- `createLoan()` — Borrow equipment with validation
- `returnEquipment()` — Return equipment
- `getUserLoans()` — Get user's loans
- `findOverdueLoan()` — Find overdue items

## UserService

Manages user authentication and profile operations.

```java
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User createUser(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }
        
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(UserRole.STUDENT);
        
        return userRepository.save(user);
    }
    
    public User updateRole(Long userId, UserRole newRole) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setRole(newRole);
        return userRepository.save(user);
    }
}
```

**Key Methods**:
- `createUser()` — Register new user
- `getUserById()` — Get user profile
- `updateRole()` — Change user role
- `deleteUser()` — Delete account

## AuthService

Handles authentication logic.

```java
@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    
    public LoginResponse authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        
        return new LoginResponse(token, user.getId(), user.getRole().toString());
    }
}
```

**Key Methods**:
- `authenticate()` — Validate credentials and return token
- `validateToken()` — Check token validity

## Key Patterns

### Transaction Management

```java
@Transactional  // All operations commit or rollback together
public Loan createLoan(...) {
    loanRepository.save(loan);
    equipmentRepository.save(equipment);
    waitingListRepository.update(equipment);
}
```

### Business Rule Validation

```java
// In service layer, before database operations
if (activeLoans >= 3) {
    throw new BadRequestException("Borrowing limit exceeded");
}
```

### Dependency Injection

```java
@Service
public class LoanServiceImpl {
    @Autowired
    private LoanRepository loanRepository;  // Auto-injected by Spring
}
```

---

**Next**: [Repositories](repositories.md) for data access details
