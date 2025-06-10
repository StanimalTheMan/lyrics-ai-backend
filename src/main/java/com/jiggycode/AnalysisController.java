package com.jiggycode;

import com.jiggycode.dto.AnalysisResponse;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<AnalysisResponse> analyze(
        @RequestBody AnalyzeRequest analyzeRequest
    ) {
        try {
            String explanation = aiService.analyzeWord(analyzeRequest.getWord(), analyzeRequest.getContext());
            AnalysisResponse response = new AnalysisResponse(explanation);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AnalysisResponse("Error analyzing word: " + e.getMessage()));
        }
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
