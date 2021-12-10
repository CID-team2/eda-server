package eda.service;

import eda.domain.DatasetColumn;
import eda.domain.worker.JobManager;
import eda.dto.JobDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class JobService {
    private final JobManager jobManager;

    public void addJobs(List<DatasetColumn> jobs) {
        jobManager.addJobs(jobs);
    }

    public List<JobDto> getWaitingJobs() {
        return jobManager.getWaitingJobs().stream()
                .map(JobDto::of)
                .toList();
    }

    @Transactional
    public boolean claimJob(long jobId) {
        return jobManager.claimJob(jobId);
    }

    public boolean finishJob(long jobId) {
        return jobManager.finishJob(jobId);
    }
}
