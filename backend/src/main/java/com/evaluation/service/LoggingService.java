package com.evaluation.service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class LoggingService {

    private final DynamoDB dynamoDB;
    private final Table errorLogTable;
    private final Table infoLogTable;

    public LoggingService() {
        DynamoDB tempDynamoDB = null;
        Table tempErrorLogTable = null;
        Table tempInfoLogTable = null;

        try {
            AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                    .withRegion("us-east-1") // Configure your AWS region
                    .build();
            tempDynamoDB = new DynamoDB(client);
            tempErrorLogTable = tempDynamoDB.getTable("ErrorLogs");
            tempInfoLogTable = tempDynamoDB.getTable("InfoLogs");
        } catch (Exception e) {
            // Fallback to local logging if DynamoDB is not available
            System.err.println("DynamoDB not available, using local logging: " + e.getMessage());
            // tempDynamoDB, tempErrorLogTable, tempInfoLogTable remain null
        }

        this.dynamoDB = tempDynamoDB;
        this.errorLogTable = tempErrorLogTable;
        this.infoLogTable = tempInfoLogTable;
    }

    public void logError(String message, String error, String details) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String logId = UUID.randomUUID().toString();

        if (errorLogTable != null) {
            try {
                Item item = new Item()
                        .withPrimaryKey("logId", logId)
                        .withString("timestamp", timestamp)
                        .withString("level", "ERROR")
                        .withString("message", message)
                        .withString("error", error)
                        .withString("details", details);

                errorLogTable.putItem(item);
            } catch (Exception e) {
                System.err.println("Failed to log to DynamoDB: " + e.getMessage());
                logToConsole("ERROR", message, error, details, timestamp);
            }
        } else {
            logToConsole("ERROR", message, error, details, timestamp);
        }
    }

    public void logInfo(String message, String details) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String logId = UUID.randomUUID().toString();

        if (infoLogTable != null) {
            try {
                Item item = new Item()
                        .withPrimaryKey("logId", logId)
                        .withString("timestamp", timestamp)
                        .withString("level", "INFO")
                        .withString("message", message)
                        .withString("details", details);

                infoLogTable.putItem(item);
            } catch (Exception e) {
                System.err.println("Failed to log to DynamoDB: " + e.getMessage());
                logToConsole("INFO", message, "", details, timestamp);
            }
        } else {
            logToConsole("INFO", message, "", details, timestamp);
        }
    }

    private void logToConsole(String level, String message, String error, String details, String timestamp) {
        System.out.println(String.format("[%s] %s - %s | %s | %s", timestamp, level, message, error, details));
    }
}