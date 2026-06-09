package com.footballadvisor.service;

import com.footballadvisor.entity.ClubEntity;
import com.footballadvisor.entity.TransferNeedEntity;
import com.footballadvisor.repository.ClubRepository;
import com.footballadvisor.repository.TransferNeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransferNeedService {

    private final TransferNeedRepository transferNeedRepository;
    private final ClubRepository clubRepository;

    public TransferNeedEntity createTransferNeed(Long clubId, TransferNeedEntity transferNeed) {
        ClubEntity club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club not found with id: " + clubId));

        transferNeed.setClub(club);

        return transferNeedRepository.save(transferNeed);
    }

    public TransferNeedEntity getTransferNeedById(Long id) {
        return transferNeedRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer need not found with id: " + id));
    }
}
