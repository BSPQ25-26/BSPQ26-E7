# Repositories & Validation

## Repository Layer (Spring Data JPA)

Spring Data JPA repositories handle all database operations without writing SQL.

### EquipmentRepository

```java
@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    
    List<Equipment> findByStatus(EquipmentStatus status);
    
    @Query("SELECT e FROM Equipment e WHERE e.status = ?1 AND e.type = ?2")
    List<Equipment> findAvailableByType(EquipmentStatus status, String type);
    
    boolean existsByNameIgnoreCase(String name);
}
```

### LoanRepository

```java
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    
    List<Loan> findByUserIdAndStatus(Long userId, LoanStatus status);
    
    List<Loan> findByEquipmentId(Long equipmentId);
    
    @Query("SELECT l FROM Loan l WHERE l.plannedReturnDate < CURDATE() AND l.status = 'ACTIVE'")
    List<Loan> findOverdueLoans();
    
    @Query("SELECT l FROM Loan l WHERE l.userId = ?1 AND l.plannedReturnDate < CURDATE() AND l.status = 'ACTIVE'")
    List<Loan> findOverdueLoansByUserId(Long userId);
    
    long countByUserIdAndStatus(Long userId, LoanStatus status);
}
```

### UserRepository

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<User> findByRole(UserRole role);
}
```

## Validation Rules

### Bean Validation Annotations

Validation happens automatically when saving entities:

```java
@Entity
public class Equipment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Equipment name is required")
    @Size(min = 1, max = 255, message = "Name must be 1-255 characters")
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
    @NotBlank(message = "Equipment type is required")
    @Size(min = 1, max = 255)
    @Column(name = "type", nullable = false)
    private String type;
    
    @Size(max = 1000, message = "Description too long")
    @Column(name = "description")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EquipmentStatus status = EquipmentStatus.AVAILABLE;
}
```

```java
@Entity
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 255)
    @Column(name = "name", nullable = false)
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Column(name = "password", nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.STUDENT;
}
```

### Custom Validators

For complex validations:

```java
@Component
public class LoanValidator {
    
    public void validateBorrowingRequest(User user, Equipment equipment, LocalDate returnDate) {
        // Equipment must be available
        if (!equipment.getStatus().equals(EquipmentStatus.AVAILABLE)) {
            throw new BadRequestException("Equipment not available");
        }
        
        // Return date must be 1-14 days from today
        long daysUntilReturn = ChronoUnit.DAYS.between(LocalDate.now(), returnDate);
        if (daysUntilReturn < 1 || daysUntilReturn > 14) {
            throw new BadRequestException("Loan duration must be 1-14 days");
        }
        
        // User cannot exceed borrowing limit
        long activeLoans = loanRepository.countByUserIdAndStatus(user.getId(), LoanStatus.ACTIVE);
        if (activeLoans >= 3) {
            throw new BadRequestException("Borrowing limit exceeded (max 3)");
        }
        
        // User cannot have overdue loans
        List<Loan> overdueLoans = loanRepository.findOverdueLoansByUserId(user.getId());
        if (!overdueLoans.isEmpty()) {
            throw new BadRequestException("User has overdue loans");
        }
    }
}
```

### Validation in Controllers

```java
@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {
    
    @PostMapping
    public ResponseEntity<?> createEquipment(
            @Valid @RequestBody EquipmentDTO dto) {  // @Valid triggers validation
        Equipment equipment = new Equipment();
        equipment.setName(dto.getName());
        equipment.setType(dto.getType());
        // ...
        return ResponseEntity.ok(equipmentService.createEquipment(equipment));
    }
}
```

---

## Key Takeaways

| Component | Purpose | Example |
|-----------|---------|---------|
| **Repository** | Abstract data access | `userRepository.findByEmail(email)` |
| **Bean Validation** | Automatic input validation | `@NotBlank`, `@Email`, `@Size` |
| **Custom Validators** | Complex business rules | Check borrowing limit, equipment status |
| **@Transactional** | Database transactions | All-or-nothing operations |

---

**Next**: [Database Schema](../database/schema.md)
