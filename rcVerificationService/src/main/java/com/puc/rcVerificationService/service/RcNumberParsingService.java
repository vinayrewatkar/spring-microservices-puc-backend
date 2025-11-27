package com.puc.rcVerificationService.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RcNumberParsingService {

    // Same words as your JS Set
    private static final Set<String> WORD_SET = new HashSet<>(Arrays.asList(
            "IND",
            " ",
            "-",
            "_",
            "MARUTI SUZUKI",
            "HYUNDAI",
            "TATA MOTORS",
            "MAHINDRA & MAHINDRA",
            "TOYOTA",
            "HONDA",
            "FORD",
            "RENAULT",
            "NISSAN",
            "VOLKSWAGEN",
            "MERCEDES-BENZ",
            "BMW",
            "AUDI",
            "SKODA",
            "VOLVO",
            "JEEP",
            "KIA",
            "MG MOTOR",
            "JAGUAR LAND ROVER",
            "FIAT",
            "LAMBORGHINI",
            "PORSCHE",
            "ROLLS-ROYCE",
            "BENTLEY",
            "ASTON MARTIN",
            "FERRARI",
            "MASERATI",
            "ISUZU",
            "FORCE MOTORS",
            "PREMIER",
            "BAJAJ AUTO",
            "TVS MOTORS",
            "HERO MOTOCORP",
            "ROYAL ENFIELD",
            "MAHINDRA TWO WHEELERS",
            "YAMAHA",
            "SUZUKI MOTORCYCLE",
            "KAWASAKI",
            "TRIUMPH MOTORCYCLES",
            "HARLEY-DAVIDSON",
            "HYOSUNG",
            "INDIAN MOTORCYCLE",
            "PIAGGIO",
            "DUCATI",
            "APRILIA",
            "BENELLI",
            "MV AGUSTA",
            "NORTON",
            "HUSQVARNA",
            "BMW MOTORRAD",
            "KTM",
            "JAWA",
            "TRIUMPH MOTORCYCLES",
            "KYMCO",
            "KAUN",
            "OLA ELECTRIC"
    ));

    /**
     * Java equivalent of JS parseRcNumber(extractedText: string[])
     * @param extractedText list of OCR text blocks (each may contain multiple lines separated by '\n')
     * @return cleaned candidate RC numbers
     */
    public List<String> parseRcNumber(List<String> extractedText) {
        List<String> resultArr = new ArrayList<>();

        for (String textBlock : extractedText) {
            if (textBlock == null || textBlock.isEmpty()) {
                continue;
            }

            String[] textLines = textBlock.split("\\r?\\n");
            StringBuilder result = new StringBuilder();

            for (String line : textLines) {
                String trimmed = line.trim();
                if (!WORD_SET.contains(trimmed)
                        && trimmed.length() >= 2
                        && trimmed.length() <= 10) {
                    result.append(trimmed);
                }
            }

            String cleaned = result.toString()
                    .replace(" ", "")
                    .replace(".", "")
                    .replace("-", "");

            if (!cleaned.isEmpty()) {
                resultArr.add(cleaned);
            }
        }

        return resultArr;
    }

    /**
     * Convenience method: return the first candidate or throw if none.
     */
    public String extractSingleRcNumber(List<String> extractedText) {
        List<String> candidates = parseRcNumber(extractedText);
        if (candidates.isEmpty()) {
            throw new RuntimeException("No RC number candidate found after parsing OCR text");
        }
        // you can add more logic to pick the best candidate here
        return candidates.get(0);
    }
}
