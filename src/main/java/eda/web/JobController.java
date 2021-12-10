package eda.web;

import eda.dto.JobDto;
import eda.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/job")
@RestController
public class JobController {
    private final JobService jobService;

    @GetMapping
    public List<JobDto> getJobList() {
        return jobService.getWaitingJobs();
    }

    @PutMapping("/{jobId}")
    public boolean claimJob(@PathVariable Long jobId) {
        return jobService.claimJob(jobId);
    }

    @DeleteMapping("/{jobId}")
    public boolean finishJob(@PathVariable Long jobId) {
        return jobService.finishJob(jobId);
    }
}
