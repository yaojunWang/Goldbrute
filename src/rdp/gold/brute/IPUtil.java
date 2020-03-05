package rdp.gold.brute;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import org.apache.commons.net.util.SubnetUtils;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class IPUtil {
    public static List<String> getIpList(String minIp, String maxIp) {
        List<String> iplist = new ArrayList();

        long minIpLong = ipToLong(minIp);
        long maxIpLong = ipToLong(maxIp);
        for (long ipItem = minIpLong; ipItem <= maxIpLong; ipItem += 1L) {
            iplist.add(longToIp(ipItem));
        }

        return iplist;
    }

    public static List<String> getIpList(String cidr) {
        SubnetUtils subnetUtils = new SubnetUtils(cidr);

        List<String> ipList = new ArrayList(java.util.Arrays.asList(subnetUtils.getInfo().getAllAddresses()));

        return ipList;
    }

    public static long cntIpByIpRangeOrCidr(String ipRangeOrCidr) {
        Matcher matcherRange = Config.PATTERN_IS_RANGE.matcher(ipRangeOrCidr);
        Matcher matcherCidr = Config.PATTERN_IS_CIDR.matcher(ipRangeOrCidr);

        long cnt = 0L;

        if (matcherRange.find()) {
            cnt += getIpList(matcherRange.group(1), matcherRange.group(2)).size();
        } else if (matcherCidr.find()) {
            SubnetUtils subnetUtils = new SubnetUtils(ipRangeOrCidr);
            cnt += subnetUtils.getInfo().getAddressCountLong();
        }

        return cnt;
    }

    public static long ipToLong(String ipAddress) {
        long result = 0L;

        String[] ipAddressInArray = ipAddress.split("\\.");

        for (int i = 3; i >= 0; i--) {
            long ip = Long.parseLong(ipAddressInArray[(3 - i)]);

            result |= ip << i * 8;
        }

        return result;
    }

    public static String longToIp(long ip) {
        StringBuilder sb = new StringBuilder(15);

        for (int i = 0; i < 4; i++) {
            sb.insert(0, Long.toString(ip & 0xFF));

            if (i < 3) {
                sb.insert(0, '.');
            }

            ip >>= 8;
        }

        return sb.toString();
    }
}
