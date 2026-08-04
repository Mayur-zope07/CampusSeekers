package com.campusseekers.service.impl;

import com.campusseekers.dto.CutoffRequest;
import com.campusseekers.dto.CutoffResponse;
import com.campusseekers.entity.CollegeBranch;
import com.campusseekers.entity.Cutoff;
import com.campusseekers.exception.DuplicateResourceException;
import com.campusseekers.exception.ResourceNotFoundException;
import com.campusseekers.mapper.CutoffMapper;
import com.campusseekers.repository.CollegeBranchRepository;
import com.campusseekers.repository.CutoffRepository;
import com.campusseekers.service.CutoffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CutoffServiceImpl implements CutoffService {

    private final CutoffRepository cutoffRepository;
    private final CollegeBranchRepository collegeBranchRepository;
    private final CutoffMapper cutoffMapper;

    @Override
    @Transactional
    public CutoffResponse createCutoff(CutoffRequest request) {
        CollegeBranch collegeBranch = collegeBranchRepository.findById(request.getCollegeBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("College-Branch mapping not found with ID: " + request.getCollegeBranchId()));

        if (cutoffRepository.existsByCollegeBranchIdAndExamNameAndYearAndRoundAndCategoryAndSeatType(
                request.getCollegeBranchId(), request.getExamName(), request.getYear(), request.getRound(), request.getCategory(), request.getSeatType())) {
            throw new DuplicateResourceException("Cutoff record already exists for the given combination of College-Branch, Exam, Year, Round, Category, and Seat Type");
        }

        Cutoff cutoff = cutoffMapper.toEntity(request);
        cutoff.setCollegeBranch(collegeBranch);

        Cutoff savedCutoff = cutoffRepository.save(cutoff);
        return cutoffMapper.toResponse(savedCutoff);
    }

    @Override
    @Transactional
    public CutoffResponse updateCutoff(UUID id, CutoffRequest request) {
        Cutoff cutoff = cutoffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cutoff record not found with ID: " + id));

        CollegeBranch collegeBranch = collegeBranchRepository.findById(request.getCollegeBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("College-Branch mapping not found with ID: " + request.getCollegeBranchId()));

        // Check if any unique fields are changing and if the new combination collides
        boolean hasChanged = !cutoff.getCollegeBranch().getId().equals(request.getCollegeBranchId())
                || !cutoff.getExamName().equals(request.getExamName())
                || !cutoff.getYear().equals(request.getYear())
                || !cutoff.getRound().equals(request.getRound())
                || !cutoff.getCategory().equals(request.getCategory())
                || !cutoff.getSeatType().equals(request.getSeatType());

        if (hasChanged && cutoffRepository.existsByCollegeBranchIdAndExamNameAndYearAndRoundAndCategoryAndSeatType(
                request.getCollegeBranchId(), request.getExamName(), request.getYear(), request.getRound(), request.getCategory(), request.getSeatType())) {
            throw new DuplicateResourceException("Cutoff record already exists for the given combination of College-Branch, Exam, Year, Round, Category, and Seat Type");
        }

        cutoffMapper.updateCutoffFromRequest(request, cutoff);
        cutoff.setCollegeBranch(collegeBranch);

        Cutoff updatedCutoff = cutoffRepository.save(cutoff);
        return cutoffMapper.toResponse(updatedCutoff);
    }

    @Override
    @Transactional
    public void deleteCutoff(UUID id) {
        Cutoff cutoff = cutoffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cutoff record not found with ID: " + id));
        cutoffRepository.delete(cutoff);
    }

    @Override
    @Transactional(readOnly = true)
    public CutoffResponse getCutoffById(UUID id) {
        Cutoff cutoff = cutoffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cutoff record not found with ID: " + id));
        return cutoffMapper.toResponse(cutoff);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CutoffResponse> getAllCutoffs() {
        List<Cutoff> cutoffs = cutoffRepository.findAll();
        return cutoffMapper.toResponseList(cutoffs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CutoffResponse> getCutoffsByCollegeBranch(UUID collegeBranchId) {
        List<Cutoff> cutoffs = cutoffRepository.findByCollegeBranchIdOrderByYearDesc(collegeBranchId);
        return cutoffMapper.toResponseList(cutoffs);
    }
}
