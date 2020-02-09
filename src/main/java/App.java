import org.joda.time.DateTime;
import org.joda.time.Seconds;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class App {

    static final String INPUT_FILE_NAME = "src/main/resources/test3.log";
    static final Pattern MATCH_SERVICE_NAME = Pattern.compile("\\((.*?):");
    static final Pattern MATCH_REQUEST_ID = Pattern.compile("(.*?\\(.*?)(\\d+)");
    static final Pattern MATCH_DATE = Pattern.compile("(.*?)(.?TRACE)(.*)");

    public static void main(String[] args) throws IOException {
        returnTotalNumberOfMatchingLines(INPUT_FILE_NAME);
    }

    public static void returnTotalNumberOfMatchingLines(String inputFile) throws IOException {
        List<String> serviceNames;
        List<String> requestIds;
        List<String> listDates;
        long requestsNumber;
        int maxTime;

        try (Stream<String> fileStream = Files.lines(Paths.get(inputFile))) {
            serviceNames = fileStream
                    .map(x -> MATCH_SERVICE_NAME.matcher(x))
                    .filter(Matcher::find)
                    .map(x -> x.group(1))
                    .distinct()
                    .collect(Collectors.toList());
        }

        for(String serviceName : serviceNames){
            try (Stream<String> fileStream = Files.lines(Paths.get(inputFile))) {
                requestsNumber = fileStream
                        .filter(s -> s.contains(serviceName))
                        .map(x -> MATCH_REQUEST_ID.matcher(x))
                        .filter(Matcher::find)
                        .map(x -> x.group(2))
                        .distinct()
                        .count();
            }

            try (Stream<String> fileStream = Files.lines(Paths.get(inputFile))) {
                requestIds = fileStream
                    .filter(s -> s.contains(serviceName))
                    .map(x -> MATCH_REQUEST_ID.matcher(x))
                    .filter(Matcher::find)
                    .map(x -> x.group(2))
                    .distinct()
                    .collect(Collectors.toList());
            }

            maxTime = 0;

            for(String IDs: requestIds){

                try (Stream<String> fileStream = Files.lines(Paths.get(inputFile))) {
                    listDates = fileStream
                                    .filter(s -> s.contains(IDs))
                                    .map(x -> MATCH_DATE.matcher(x))
                                    .filter(Matcher::find)
                                    .map(x -> x.group(1))
                                    .collect(Collectors.toList());
                }

                if (listDates.size() == 1) continue;

                int timeDiffSeconds = getDiffSeconds(listDates.get(0), listDates.get(1));

                if (maxTime < timeDiffSeconds) {
                    maxTime = timeDiffSeconds;
                }
            }

            System.out.println("Service name: " + serviceName +
                    " | Amount of requests: " + requestsNumber +
                    " | maxTimeExecution: " + maxTime);
            System.out.println();
        }
    }

    public static int getDiffSeconds(String start, String end) {
        DateTime startDateTime = DateTime.parse(start);
        DateTime endDateTime = DateTime.parse(end);
        return Seconds.secondsBetween(startDateTime, endDateTime).getSeconds();
    }
}
