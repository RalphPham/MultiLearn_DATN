package org.example.multileanproject.service;

import lombok.RequiredArgsConstructor;
import org.example.multileanproject.dto.AdminAccountDTO;
import org.example.multileanproject.entity.Admin;
import org.example.multileanproject.entity.AdminPermission;
import org.example.multileanproject.entity.AdminRole;
import org.example.multileanproject.repository.AdminRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAccountService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminActionLogService logService;

    public List<AdminAccountDTO> getAll() {
        return adminRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdminAccountDTO create(AdminAccountDTO dto, String createdBy) {
        if (adminRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("Username da ton tai: " + dto.getUsername());
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new RuntimeException("Mat khau khong duoc de trong.");
        }

        AdminRole role = parseRole(dto.getAdminRole());
        EnumSet<AdminPermission> permissions = resolvePermissions(role, dto.getPermissions(), null);

        Admin admin = Admin.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName())
                .adminRole(role)
                .permissions(AdminPermission.toCsv(permissions))
                .isActive(true)
                .build();

        Admin saved = adminRepository.save(admin);
        logService.log("CREATE_ADMIN",
                "Tao tai khoan admin: " + saved.getUsername() + " (" + role.name() + ")",
                saved.getId(), "ADMIN");
        return toDTO(saved);
    }

    @Transactional
    public AdminAccountDTO update(Long id, AdminAccountDTO dto, String updatedBy) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay admin ID: " + id));

        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            admin.setFullName(dto.getFullName());
        }

        AdminRole nextRole = admin.getAdminRole() != null ? admin.getAdminRole() : AdminRole.STAFF;
        if (dto.getAdminRole() != null) {
            AdminRole newRole = parseRole(dto.getAdminRole());
            if (admin.getAdminRole() == AdminRole.SUPER_ADMIN && newRole == AdminRole.STAFF) {
                long count = adminRepository.countByAdminRole(AdminRole.SUPER_ADMIN);
                if (count <= 1) {
                    throw new RuntimeException("Khong the ha quyen: he thong phai co it nhat 1 Super Admin.");
                }
            }
            admin.setAdminRole(newRole);
            nextRole = newRole;
        }

        if (dto.getIsActive() != null) {
            admin.setIsActive(dto.getIsActive());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        EnumSet<AdminPermission> permissions = resolvePermissions(
                nextRole,
                dto.getPermissions(),
                admin.getPermissions()
        );
        admin.setPermissions(AdminPermission.toCsv(permissions));

        Admin saved = adminRepository.save(admin);
        logService.log("UPDATE_ADMIN",
                "Cap nhat tai khoan admin: " + saved.getUsername(),
                id, "ADMIN");
        return toDTO(saved);
    }

    @Transactional
    public void delete(Long id, String deletedBy) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay admin ID: " + id));

        if (admin.getUsername().equals(deletedBy)) {
            throw new RuntimeException("Khong the xoa tai khoan cua chinh minh.");
        }

        if (admin.getAdminRole() == AdminRole.SUPER_ADMIN) {
            long count = adminRepository.countByAdminRole(AdminRole.SUPER_ADMIN);
            if (count <= 1) {
                throw new RuntimeException("Khong the xoa: he thong phai co it nhat 1 Super Admin.");
            }
        }

        adminRepository.deleteById(id);
        logService.log("DELETE_ADMIN",
                "Xoa tai khoan admin: " + admin.getUsername(),
                id, "ADMIN");
    }

    private AdminRole parseRole(String roleStr) {
        if (roleStr == null) return AdminRole.STAFF;
        try {
            return AdminRole.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Role khong hop le: " + roleStr);
        }
    }

    private EnumSet<AdminPermission> resolvePermissions(
            AdminRole role,
            List<String> requestedPermissions,
            String storedPermissions
    ) {
        if (role == AdminRole.SUPER_ADMIN) {
            return AdminPermission.allPermissions();
        }

        if (requestedPermissions != null) {
            EnumSet<AdminPermission> parsed = AdminPermission.fromNames(requestedPermissions);
            if (parsed.isEmpty()) {
                return AdminPermission.defaultStaffPermissions();
            }
            return parsed;
        }

        EnumSet<AdminPermission> current = AdminPermission.fromCsv(storedPermissions);
        if (current.isEmpty()) {
            return AdminPermission.defaultStaffPermissions();
        }
        return current;
    }

    private AdminAccountDTO toDTO(Admin a) {
        EnumSet<AdminPermission> permissions = a.getAdminRole() == AdminRole.SUPER_ADMIN
                ? AdminPermission.allPermissions()
                : AdminPermission.fromCsv(a.getPermissions());

        if (permissions.isEmpty() && a.getAdminRole() != AdminRole.SUPER_ADMIN) {
            permissions = AdminPermission.defaultStaffPermissions();
        }

        return AdminAccountDTO.builder()
                .id(a.getId())
                .username(a.getUsername())
                .fullName(a.getFullName())
                .adminRole(a.getAdminRole() != null ? a.getAdminRole().name() : "STAFF")
                .isActive(a.getIsActive())
                .permissions(AdminPermission.toNames(permissions))
                .createdAt(a.getCreatedAt())
                .build();
    }
}