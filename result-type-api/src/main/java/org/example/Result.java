package org.example;

public sealed interface Result<T, E extends IError> permits Result.Ok, Result.Err {

    T tryGet();

    T unwrap();

    Result<T, ? extends IError> errorContext(String s);
    record Ok<T, E extends IError>(T data) implements Result<T, E> {
        @Override
        public T tryGet() {
            return data;
        }

        @Override
        public T unwrap() {
            return data;
        }

        @Override
        public Result<T, ? extends IError> errorContext(String s) {
            return this;
        }
    }

    record Err<T, E extends IError>(E error) implements Result<T, E> {
        @Override
        public T tryGet() {
            throw new ResultException(error.toString());
        }

        @Override
        public T unwrap() {
            throw new ResultException(error.toString());
        }

        @Override
        public Result<T, ? extends IError> errorContext(String s) {
            return new Result.Err<>(error.context(s));
        }

        public static <T, E extends IError> Result<T, E> fromError(E error) {
            return new Result.Err<>(error);
        }
    }
}
