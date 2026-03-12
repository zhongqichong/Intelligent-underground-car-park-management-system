package com.garage.service;

import com.garage.entity.Alert;
import com.garage.entity.User;
import com.garage.repository.AlertRepository;
import com.garage.repository.MapElementRepository;
import com.garage.repository.ParkingSpotRepository;
import com.garage.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AdminServiceTest {

    @Test
    void shouldRejectDuplicateAdminUsername() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
        ParkingSpotRepository spotRepository = Mockito.mock(ParkingSpotRepository.class);
        MapElementRepository mapElementRepository = Mockito.mock(MapElementRepository.class);
        AlertRepository alertRepository = Mockito.mock(AlertRepository.class);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(new User()));
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        AdminService service = new AdminService(userRepository, passwordEncoder, spotRepository, mapElementRepository, alertRepository);
        assertThrows(IllegalArgumentException.class, () -> service.createAdmin("admin", "Admin@12345"));
    }

    @Test
    void shouldNotExposePasswordInAdminResponse() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
        ParkingSpotRepository spotRepository = Mockito.mock(ParkingSpotRepository.class);
        MapElementRepository mapElementRepository = Mockito.mock(MapElementRepository.class);
        AlertRepository alertRepository = Mockito.mock(AlertRepository.class);

        when(userRepository.findByUsername("newadmin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        AdminService service = new AdminService(userRepository, passwordEncoder, spotRepository, mapElementRepository, alertRepository);
        var response = service.createAdmin("newadmin", "Admin@12345");

        assertEquals(1L, response.id());
        assertEquals("newadmin", response.username());
    }

    @Test
    void shouldResolveAlert() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
        ParkingSpotRepository spotRepository = Mockito.mock(ParkingSpotRepository.class);
        MapElementRepository mapElementRepository = Mockito.mock(MapElementRepository.class);
        AlertRepository alertRepository = Mockito.mock(AlertRepository.class);

        Alert alert = Alert.builder().id(10L).resolved(false).build();
        when(alertRepository.findById(10L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(Alert.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminService service = new AdminService(userRepository, passwordEncoder, spotRepository, mapElementRepository, alertRepository);
        var response = service.resolveAlert(10L);

        assertEquals(true, response.resolved());
    }
}
