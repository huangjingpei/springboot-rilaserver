package com.graddu.rilaserver.controller;

import com.graddu.rilaserver.service.LiveStreamConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/live")
public class LiveStreamLogController {
    @Autowired
    private LiveStreamConfigService liveStreamConfigService;

    @GetMapping("/status/client-logs")
    public ResponseEntity<?> getClientLogs(@RequestParam String clientId,
                                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime since) {
        List<LiveStreamConfigService.StatusLogDto> logs = liveStreamConfigService.getClientStatusLogs(clientId);
        if (since != null) {
            logs = logs.stream().filter(l -> {
                LocalDateTime t = LocalDateTime.parse(l.getChangeTime().replace(" ", "T"));
                return !t.isBefore(since);
            }).collect(Collectors.toList());
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("clientId", clientId);
        resp.put("logs", logs);
        resp.put("count", logs.size());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/sessions")
    public ResponseEntity<?> getClientSessions(@RequestParam String clientId,
                                               @RequestParam(required = false, defaultValue = "7") int days) {
        List<LiveStreamConfigService.StatusLogDto> logs = liveStreamConfigService.getClientStatusLogs(clientId);
        List<LiveStreamConfigService.StatusLogDto> sorted = logs.stream()
                .sorted(Comparator.comparing(LiveStreamConfigService.StatusLogDto::getChangeTime))
                .collect(Collectors.toList());
        Map<Long, LocalDateTime> startMap = new HashMap<>();
        List<Map<String, Object>> sessions = new ArrayList<>();
        for (LiveStreamConfigService.StatusLogDto l : sorted) {
            LocalDateTime ts = LocalDateTime.parse(l.getChangeTime().replace(" ", "T"));
            Long cfg = l.getConfigId();
            Boolean newStatus = l.getNewStatus();
            if (Boolean.TRUE.equals(newStatus)) {
                startMap.put(cfg, ts);
            } else {
                if (startMap.containsKey(cfg)) {
                    LocalDateTime st = startMap.remove(cfg);
                    long dur = Duration.between(st, ts).getSeconds();
                    Map<String, Object> s = new LinkedHashMap<>();
                    s.put("clientId", l.getClientId());
                    s.put("configId", cfg);
                    s.put("startTime", st.toString().replace("T", " "));
                    s.put("endTime", ts.toString().replace("T", " "));
                    s.put("durationSeconds", Math.max(dur, 0));
                    sessions.add(s);
                }
            }
        }
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        List<Map<String, Object>> filtered = sessions.stream().filter(s -> {
            LocalDateTime end = LocalDateTime.parse(((String) s.get("endTime")).replace(" ", "T"));
            return !end.isBefore(cutoff);
        }).collect(Collectors.toList());
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("clientId", clientId);
        resp.put("sessions", filtered);
        resp.put("count", filtered.size());
        return ResponseEntity.ok(resp);
    }
}
