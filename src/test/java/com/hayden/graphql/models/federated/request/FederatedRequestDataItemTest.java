package com.hayden.graphql.models.federated.request;

import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;

import java.util.TimerTask;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class FederatedRequestDataItemTest {

    @Test
    public void testBuild() {
        FederatedRequestDataItem build = FederatedRequestDataItem.builder()
                .path("/")
                .build();
    }

    class TestClass {
        public void print() {
            System.out.println(this.toString());
        }
    }

    class TestClassExtend extends TestClass {
        @Override
        public void print() {
            System.out.println("Extend");
        }
    }
    class TestClassTwo extends TestClass {
        @Override
        public void print() {
            System.out.println("Extend");
        }
        public void printTwo() {
//            System.out.println("Extend");
        }
    }

    @Test
    public void testEquals() {
        IntStream.range(0, 20).forEach(i -> this.doFirst());
        IntStream.range(0, 20).forEach(i -> this.doSecond());

        System.out.println("STARTING");

        this.doFirst();

        System.out.println("SECOND");

        this.doSecond();
    }

    private void doFirst() {
        StopWatch watch = new StopWatch();
        watch.start();
        IntStream.range(0, 100000).boxed().forEach(i -> {
            TestClass c;
            if (i % 2 == 0) {
                c = new TestClassTwo();
            } else {
                c = new TestClassExtend() ;
            }

            if (i % 2 == 0 && c instanceof TestClassTwo two) {
                two.printTwo();
            }
        });
        watch.stop();
        System.out.println(watch.prettyPrint());
    }

    void doSecond() {
        StopWatch watch = new StopWatch();
        watch.start();
        IntStream.range(0, 100000).boxed().forEach(i -> {
            TestClass c;
            if (i % 2 == 0) {
                c = new TestClassTwo();
            } else {
                c = new TestClassExtend() ;
            }

            if (i % 2 == 0) {
                ((TestClassTwo) c).printTwo();
            }
        });
        watch.stop();
        System.out.println(watch.prettyPrint());
    }

}