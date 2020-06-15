package io;

import java.util.Scanner;
import java.util.function.Function;

public class Main {

    public static void main(String[] args) {
        IO<?> mainIO = pureMain();
        System.out.println("main io has been constructed.");
        mainIO.process();
    }

    private static IO<?> pureMain() {
        return (IO<?>) Input.readLine()
            .flatMap(Main::quotePrinter);
    }

    private static IO<Void> quotePrinter(String s) {
        String log = String.format("input was: %s", s);
        return Output.print(log);
    }

    private static IO<Integer> add(int a, int b) {
        String log = String.format("adding two integers %s and %s = %s", a, b, a+b);
        return Output.print(log, a+b);
    }
}

class Output<T> extends IO<T> {
    String log;
    IO<T> next;

    private Output(String log, IO<T> next) {
        this.log = log;
        this.next = next;
    }
    
    private Output(String log, T carry) {
        this(log, new ConstantIO<>(carry));
    }

    private Output(String log) {
        this(log, (T) null);
    }

    static <T> Output<T> print(String log, T output) {
        return new Output<>(log, output);
    }

    static Output<Void> print(String log) {
        return new Output<>(log);
    }

    @Override
    public <R> IO<R> flatMap(Function<T, Monad<R>> f) {
        return new Output<>(log, (IO<R>)next.flatMap(f));
    }

    @Override
    public <R> IO<R> map(Function<T, R> f) {
        return new Output<>(log, (IO<R>)next.map(f));
    }

    @Override
    T process() {
        System.out.println(log);
        return next.process();
    }
}

class Input<T> extends IO<T> {

    Function<String, IO<T>> inputProcessor;

    private Input(Function<String, IO<T>> f) {
        inputProcessor = f;
    }

    static Input<String> readLine() {
        return new Input<>(s -> new ConstantIO<>(s));
    }

    @Override
    public <R> Input<R> flatMap(Function<T, Monad<R>> f) {
        return new Input<>(i -> (IO<R>)inputProcessor.apply(i).flatMap(f));
    }

    @Override
    public <R> Input<R> map(Function<T, R> f) {
        return new Input<>(i -> (IO<R>)inputProcessor.apply(i).map(f));
    }

    @Override
    T process() {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        scanner.close();
        return inputProcessor.apply(input).process();
    }
}

class ConstantIO<T> extends IO <T> {

    T value;

    ConstantIO(T t) {
        value = t;
    }

    @Override
    public <R> IO<R> flatMap(Function<T, Monad<R>> f) {
        return (IO<R>)f.apply(value);
    }

    @Override
    public <R> ConstantIO<R> map(Function<T, R> f) {
        return new ConstantIO<>(f.apply(value));
    }

    @Override
    T process() {
        return value;
    }
    
}

abstract class IO<T> implements Monad<T>, Functor<T> {

    abstract T process();
}

interface Functor<T> {
    
    public <R> Functor<R> map(Function<T, R> f);
}

interface Monad<T> {
    
    public <R> Monad<R> flatMap(Function<T, Monad<R>> f);
}
