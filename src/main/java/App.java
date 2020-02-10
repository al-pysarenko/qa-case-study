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

    static final String INPUT_FILE_NAME = "src/main/resources/test.log";

    static final Pattern MATCH_SERVICE_NAME = Pattern.compile("\\((.*?):");
    static final Pattern MATCH_REQUEST_ID = Pattern.compile("(.*?\\(.*?)(\\d+)");
    static final Pattern MATCH_DATE = Pattern.compile("(.*?)(.?TRACE)(.*)");

    static List<String> allFileList;
    static List<String> serviceNames;
    static List<String> requestIds;
    static List<String> listTimestamps;
    static List<String> currentServiceList;

    static long amountOfRequests;
    static int maxTime;

    public static void main(String[] args) throws IOException {
        System.out.println(DateTime.now());
        parseLogFile(INPUT_FILE_NAME);
        System.out.println(DateTime.now());
    }

    public static void parseLogFile(String inputFile) throws IOException {

        try (Stream<String> fileStream = Files.lines(Paths.get(inputFile))) {
            allFileList = fileStream.collect(Collectors.toList());
        }

        serviceNames = getServiceNamesFromList(allFileList);

        for(String serviceName : serviceNames){
            amountOfRequests = getAmountOfRequestsByService(allFileList, serviceName);

            currentServiceList = allFileList.stream()
                    .filter(s -> s.contains(serviceName))
                    .collect(Collectors.toList());

            requestIds = extractRequestIdList(currentServiceList);
            maxTime = 0;

            for(String requestId : requestIds){
                listTimestamps = getTimestampsById(currentServiceList, requestId);

                if (listTimestamps.size() == 1) continue;

                int timeDiffSeconds = getDiffSeconds(listTimestamps.get(0), listTimestamps.get(1));

                if (maxTime < timeDiffSeconds) {
                    maxTime = timeDiffSeconds;
                }
            }

            System.out.println("Service name: " + serviceName +
                    " | Amount of requests: " + amountOfRequests +
                    " | maxTimeExecution: " + maxTime);
            System.out.println();
        }
    }

    public static int getDiffSeconds(String start, String end) {
        DateTime startDateTime = DateTime.parse(start);
        DateTime endDateTime = DateTime.parse(end);
        return Seconds.secondsBetween(startDateTime, endDateTime).getSeconds();
    }

    public static List<String> getTimestampsById(List<String> serviceList, String requestId) {
        return serviceList.stream()
                .filter(s -> s.contains(requestId))
                .map(MATCH_DATE::matcher)
                .filter(Matcher::find)
                .map(x -> x.group(1))
                .collect(Collectors.toList());
    }

    public static long getAmountOfRequestsByService(List<String> list, String service) {
        return list.stream()
                .filter(s -> s.contains(service))
                .map(MATCH_REQUEST_ID::matcher)
                .filter(Matcher::find)
                .map(x -> x.group(2))
                .distinct()
                .count();
    }

    public static List<String> extractRequestIdList(List<String> serviceList) {
        return serviceList.stream()
                .map(MATCH_REQUEST_ID::matcher)
                .filter(Matcher::find)
                .map(x -> x.group(2))
                .distinct()
                .collect(Collectors.toList());
    }

    public static List<String> getServiceNamesFromList(List<String> list) {
        return list.stream()
                .map(MATCH_SERVICE_NAME::matcher)
                .filter(Matcher::find)
                .map(x -> x.group(1))
                .distinct()
                .collect(Collectors.toList());
    }
}
