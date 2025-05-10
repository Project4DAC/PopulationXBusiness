
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ulpgc.BormeSubscriberDataLake.processors.ConsumptionTransformer;

import static org.junit.jupiter.api.Assertions.*;

class ConsumptionTransformerTest {
    
    private ConsumptionTransformer transformer;
    
    @BeforeEach
    void setUp() {
        transformer = new ConsumptionTransformer();
    }
    
    @Test
    void transformNotificationData() {
        // Given
        String notificationJson = """
            {
                "date": "2025-05-06",
                "url": "https://borme.example.com/doc123",
                "notification_year": "2025",
                "notification_month": "05",
                "notification_day": "06",
                "data_quality": "complete",
                "processed_at": "2025-05-06T12:30:45"
            }
            """;
        
        // When
        String result = transformer.transform(notificationJson);
        JSONObject resultJson = new JSONObject(result);
        
        // Then
        assertTrue(resultJson.has("notification"));
        assertTrue(resultJson.has("document_type"));
        assertEquals("notification", resultJson.getString("document_type"));
        assertEquals(true, resultJson.getBoolean("ready_for_analytics"));
        
        JSONObject notification = resultJson.getJSONObject("notification");
        assertEquals("2025-05-06", notification.getString("publication_date"));
        assertEquals("https://borme.example.com/doc123", notification.getString("source_url"));
    }
    
    @Test
    void transformContentData() {
        // Given
        String contentJson = """
            {
                "processed_at": "2025-05-06T14:22:33",
                "companies": [
                    {
                        "name": "Tech Solutions, S.L.",
                        "id": "B12345678",
                        "operation": "constitution"
                    },
                    {
                        "name": "Financial Services, S.A.",
                        "id": "A87654321",
                        "operation": "modification"
                    },
                    {
                        "name": "Construction Works, S.L.",
                        "id": "B11223344",
                        "operation": "dissolution"
                    }
                ],
                "statistics": {
                    "incorporations": 1,
                    "dissolutions": 1,
                    "modifications": 1
                },
                "company_count": 3
            }
            """;
        
        // When
        String result = transformer.transform(contentJson);
        JSONObject resultJson = new JSONObject(result);
        
        // Then
        assertTrue(resultJson.has("document_type"));
        assertEquals("content", resultJson.getString("document_type"));
        assertTrue(resultJson.has("companies"));
        assertTrue(resultJson.has("statistics"));
        assertEquals(3, resultJson.getInt("total_companies"));
        assertTrue(resultJson.has("company_sectors"));
        assertTrue(resultJson.has("operation_types"));
    }
    
    @Test
    void handleInvalidInput() {
        // Given
        String invalidJson = "This is not valid JSON";
        
        // When
        String result = transformer.transform(invalidJson);
        JSONObject resultJson = new JSONObject(result);
        
        // Then
        assertTrue(resultJson.has("error"));
        assertEquals("Transformation failed", resultJson.getString("error"));
        assertTrue(resultJson.has("message"));
    }
}