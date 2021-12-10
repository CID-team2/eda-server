package eda.service;

import eda.domain.DatasetColumn;
import eda.domain.StatisticEntity;
import eda.domain.StatisticEntityRepository;
import eda.domain.worker.Job;
import eda.domain.worker.JobRepository;
import eda.dto.JobDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class JobService {
    private final JobRepository jobRepository;
    private final StatisticEntityRepository statisticEntityRepository;

    public void addJobs(List<DatasetColumn> jobs) {
        jobRepository.saveAll(
                jobs.stream().map(Job::new).toList()
        );
    }

    public List<JobDto> getWaitingJobs() {
        return jobRepository.findAllByStatus(Job.Status.WAITING).stream()
                .map(JobDto::of)
                .toList();
    }

    @Transactional
    public boolean claimJob(long jobId) {
        Optional<Job> jobColumnOptional = jobRepository.findById(jobId);
        if (jobColumnOptional.isEmpty())
            return false;
        Job job = jobColumnOptional.get();
        if (job.getStatus() == Job.Status.PROCESSING)
            return false;
        else {
            job.setStatus(Job.Status.PROCESSING);
            jobRepository.save(job);
            return true;
        }
    }

    public boolean finishJob(long jobId) {
        Optional<Job> jobColumnOptional = jobRepository.findById(jobId);
        if (jobColumnOptional.isEmpty())
            return false;
        Job job = jobColumnOptional.get();
        if (job.getStatus() == Job.Status.WAITING)
            return false;

        // refuse when statistic is not calculated
        for (StatisticEntity se : statisticEntityRepository.findAllByColumn()) {
            if (se.getModifiedAt().isBefore(job.getModifiedAt()))
                return false;
        }

        jobRepository.delete(job);
        return true;
    }
}
