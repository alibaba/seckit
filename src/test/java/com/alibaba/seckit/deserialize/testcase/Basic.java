package com.alibaba.seckit.deserialize.testcase;


import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.regex.Pattern;

public class Basic implements Serializable {

    private String aStr;

    protected int anInt;

    public Integer anInteger;

    public Short aShort;

    public BigDecimal bigDecimal;
    public BigInteger bigInteger;

    // primitives
    public boolean aBoolean;
    public long aLong;
    public double aDouble;
    public float aFloat;
    public byte aByte;
    public char aChar;

    // arrays
    public int[] intArray;
    public String[] strArray;

    // collections and maps
    public List<String> stringList;
    public Set<String> stringSet;
    public Map<String, Integer> stringIntMap;

    // common JDK types covered by internal allowlist
    public Date date;
    public TimeZone timeZone;
    public Currency currency;
    public Locale locale;
    public UUID uuid;
    public StringBuilder stringBuilder;
    public StringBuffer stringBuffer;
    public Pattern pattern;
    public SimpleDateFormat simpleDateFormat;

    // atomic types
    public AtomicBoolean atomicBoolean;
    public AtomicInteger atomicInteger;
    public AtomicLong atomicLong;
    public AtomicIntegerArray atomicIntegerArray;
    public AtomicLongArray atomicLongArray;



    public Basic() {
        this.aStr = "aStr";
        this.anInt = 123123;
        this.anInteger = 1111;

        bigDecimal = new BigDecimal(123);
        bigInteger = new BigInteger(String.valueOf(123123123L));

        aBoolean = true;
        aLong = 123456789L;
        aDouble = 123.456d;
        aFloat = 12.34f;
        aByte = (byte) 7;
        aChar = 'c';

        intArray = new int[]{1, 2, 3};
        strArray = new String[]{"x", "y", "z"};

        stringList = new ArrayList<>(Arrays.asList("a", "b", "c"));
        stringSet = new HashSet<>(Arrays.asList("u", "v"));
        stringIntMap = new HashMap<>();
        stringIntMap.put("k1", 1);
        stringIntMap.put("k2", 2);

        date = new Date();
        timeZone = TimeZone.getDefault();
        currency = Currency.getInstance(Locale.US);
        locale = Locale.US;
        uuid = UUID.randomUUID();
        stringBuilder = new StringBuilder("sb");
        stringBuffer = new StringBuffer("sbf");
        pattern = Pattern.compile("^abc$");
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        atomicBoolean = new AtomicBoolean(true);
        atomicInteger = new AtomicInteger(10);
        atomicLong = new AtomicLong(100L);
        atomicIntegerArray = new AtomicIntegerArray(new int[]{1, 2});
        atomicLongArray = new AtomicLongArray(new long[]{3L, 4L});
    }


}
