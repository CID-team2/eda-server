package eda.domain.worker;

import eda.domain.DatasetColumn;
import eda.domain.StatisticEntity;
import eda.domain.StatisticEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class JobManager {
    private final JobRepository jobRepository;
    private final StatisticEntityRepository statisticEntityRepository;

    // reset job as waiting if a worker does not send 'finish' request after fetching it
    @Value("${worker.server.check.reset_sec:3600}")
    private long resetSeconds;

    public void addJobs(List<DatasetColumn> jobs) {
        jobRepository.saveAll(
                jobs.stream().map(Job::new).toList()
        );
    }

    public List<Job> getWaitingJobs() {
        return jobRepository.findAllByStatus(Job.Status.WAITING);
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
        for (StatisticEntity se : statisticEntityRepository.findAllByColumnId(job.getColumn().getId())) {
            if (se.getModifiedAt().isBefore(job.getModifiedAt()))
                return false;
        }

        jobRepository.delete(job);
        return true;
    }

    @Transactional
    @ConditionalOnWebApplication
    @Scheduled(fixedRateString = "${worker.server.check.interval:1200000}")
    public void checkJobList() {
        List<Job> jobList = jobRepository.findAll();
        for (Job job : jobList) {
            if (job.getModifiedAt().isBefore(LocalDateTime.now().minusSeconds(resetSeconds)))
                job.setStatus(Job.Status.WAITING);
        }
        jobRepository.saveAll(jobList);
    }
}
