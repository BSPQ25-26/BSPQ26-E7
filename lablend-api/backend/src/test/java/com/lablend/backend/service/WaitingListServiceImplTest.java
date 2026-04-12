package com.lablend.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.lablend.backend.entity.WaitingList;
import com.lablend.backend.repository.WaitingListRepository;
import com.lablend.backend.service.impl.WaitingListServiceImpl;

class WaitingListServiceImplTest {

    @Mock
    private WaitingListRepository waitingListRepository;

    @InjectMocks
    private WaitingListServiceImpl waitingListService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addToQueue_Success() {
        Long userId = 1L;
        Long equipmentId = 10L;
        when(waitingListRepository.existsByUserIdAndEquipmentId(userId, equipmentId)).thenReturn(false);
        when(waitingListRepository.save(any(WaitingList.class))).thenAnswer(i -> i.getArguments()[0]);

        WaitingList result = waitingListService.addToQueue(userId, equipmentId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(waitingListRepository).save(any(WaitingList.class));
    }

    @Test
    void addToQueue_Duplicate_ThrowsException() {
        when(waitingListRepository.existsByUserIdAndEquipmentId(1L, 10L)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            waitingListService.addToQueue(1L, 10L);
        });
    }

    @Test
    void getNextInLine_Success() {
        WaitingList firstInLine = new WaitingList(1L, 10L);
        when(waitingListRepository.findFirstByEquipmentIdOrderByRequestDateAsc(10L))
            .thenReturn(Optional.of(firstInLine));

        WaitingList result = waitingListService.getNextInLine(10L);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
    }

    @Test
    void removeFromQueue_Success() {
        Long userId = 1L;
        Long equipmentId = 10L;
        waitingListService.removeFromQueue(userId, equipmentId);
        verify(waitingListRepository, times(1)).deleteByUserIdAndEquipmentId(userId, equipmentId);
    }
}