package net.enjoy.springboot.registrationlogin.controller;

import net.enjoy.springboot.registrationlogin.dto.SmartCdnClientRegisterRequest;
import net.enjoy.springboot.registrationlogin.dto.SmartCdnPlayUrlResponse;
import net.enjoy.springboot.registrationlogin.dto.SmartCdnRelayRegisterRequest;
import net.enjoy.springboot.registrationlogin.service.SmartCdnService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/smartcdn")
public class SmartCdnController {

    private static final Logger log = LoggerFactory.getLogger(SmartCdnController.class);

    @Autowired
    private SmartCdnService smartCdnService;

    @PostMapping("/client/register")
    public ResponseEntity<?> registerClient(@RequestBody SmartCdnClientRegisterRequest request) {
        try {
            smartCdnService.registerClient(request);
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("SmartCDN client register failed", e);
            return ResponseEntity.status(500).body("SmartCDN client register failed");
        }
    }

    @PostMapping("/stream/relay/register")
    public ResponseEntity<?> registerRelay(@RequestBody SmartCdnRelayRegisterRequest request) {
        try {
            boolean ok = smartCdnService.registerRelayNode(request);
            Map<String, Object> body = new HashMap<>();
            body.put("success", ok);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("SmartCDN relay register failed", e);
            return ResponseEntity.status(500).body("SmartCDN relay register failed");
        }
    }

    @GetMapping("/streams/{streamId}/play-url")
    public ResponseEntity<?> getPlayUrl(@PathVariable String streamId, 
                                      @RequestParam(required = false) String lanId,
                                      @RequestParam(required = false) String exclude) {
        try {
            SmartCdnPlayUrlResponse result = smartCdnService.getBestPlayUrl(streamId, lanId, exclude);
            if (!result.isSuccess()) {
                Map<String, Object> body = new HashMap<>();
                body.put("success", false);
                body.put("streamId", result.getStreamId());
                body.put("message", result.getMessage());
                return ResponseEntity.ok(body);
            }
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("streamId", result.getStreamId());
            body.put("pullUrl", result.getPullUrl());
            body.put("platform", result.getPlatform());
            body.put("lanId", result.getLanId());
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("SmartCDN get play url failed: streamId={}", streamId, e);
            return ResponseEntity.status(500).body("SmartCDN get play url failed");
        }
    }

    @DeleteMapping("/client/{clientId}")
    public ResponseEntity<?> deleteClient(@PathVariable String clientId) {
        try {
            int deleted = smartCdnService.deleteClient(clientId);
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("clientId", clientId);
            body.put("deleted", deleted);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("SmartCDN delete client failed: clientId={}", clientId, e);
            return ResponseEntity.status(500).body("SmartCDN delete client failed");
        }
    }

    @DeleteMapping("/streams/{streamId}/relays")
    public ResponseEntity<?> deleteRelays(@PathVariable String streamId, @RequestParam(required = false) String lanId) {
        try {
            int deleted = smartCdnService.deleteRelays(streamId, lanId);
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("streamId", streamId);
            body.put("lanId", lanId);
            body.put("deleted", deleted);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("SmartCDN delete relays failed: streamId={}", streamId, e);
            return ResponseEntity.status(500).body("SmartCDN delete relays failed");
        }
    }
}
