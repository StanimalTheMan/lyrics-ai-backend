package com.jiggycode;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {
    private final AiService aiService;


    public AnalysisController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping
    public ResponseEntity<String> analyze(
        @RequestBody AnalyzeRequest analyzeRequest
    ) {
        String result = aiService.analyzeWord(analyzeRequest.getWord(), analyzeRequest.getContext());
        return ResponseEntity.ok(result);
    }

    public static class AnalyzeRequest {
        private String word;
        private String context;

        // Constructor (optional)
        public AnalyzeRequest() {}

        // Getters and setters
        public String getWord() { return word; }
        public void setWord(String word) { this.word = word; }

        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
    }
}
