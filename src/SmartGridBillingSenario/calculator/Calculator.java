package SmartGridBillingSenario.calculator;

import org.apache.commons.collections.MapUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ydai on 8/10/17.
 */
public class Calculator {

    private static final Integer rate = 10; // rate per degree

    private Map<String, Integer> memberRateMap = new HashMap<>();

    private Map<String, List<Integer>> memberRateProcessor = new HashMap<>();

    public Calculator() {
        initMemberRateProcessor();
        initMemberRateMap();
    }


    private void initMemberRateProcessor() {
        // first member Mile
        String mike = "Mike";
        List<Integer> mikeSevenDayUsage = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        memberRateProcessor.put(mike, mikeSevenDayUsage);

        // second member Jack
        String jack = "Jack";
        List<Integer> jackSevenDayUsage = Arrays.asList(2, 3, 4, 5, 6, 7, 8);
        memberRateProcessor.put(jack, mikeSevenDayUsage);

        //third member Kate
        String kate = "kate";
        List<Integer> kateSevenDayUsage = Arrays.asList(3, 4, 5, 6, 7, 8, 9);
        memberRateProcessor.put(kate, kateSevenDayUsage);

        //fourth member Nick
        String nick = "Nick";
        List<Integer> nickSevenDayUsage = Arrays.asList(4, 5, 6, 7, 8, 9, 9);
        memberRateProcessor.put(nick, nickSevenDayUsage);
    }

    private void initMemberRateMap() {

        if (MapUtils.isNotEmpty(memberRateProcessor)) {
            memberRateMap = memberRateProcessor.entrySet().parallelStream().collect(Collectors.toMap(x -> x.getKey(), y -> calculateRate(y.getValue())));
        }
    }

    private Integer calculateRate(List<Integer> value) {
        return rate * value.stream().mapToInt(i -> i.intValue()).sum();
    }

    public Map<String, Integer> getMemberRateMap() {
        return memberRateMap;
    }
}
