package org.example;


public class Main {

    public static void main(String[] args) {
        Result<Test, ? extends IError> main = main2(Boolean.parseBoolean(args[0]));
        switch (main) {
            case Result.Ok(Test s) -> System.out.println(s.test());
            case Result.Err(IError t) -> System.out.println(t);
        }
    }

    public static Result<Test, StdError> main2(boolean err) {
        return new Result.Ok<>(main(err).errorContext("got error while trying main(bool)").tryGet());
    }

    public static <T extends IError> Result<Test, StdError> main(boolean err) {
        Result<Test, StdError> res;
        if (!err)
            res = getRes("Hello");
        else {
            res = getError();
        }
        res.errorContext("Failed to get hello").tryGet();
        return res;
    }

    public static Result<Test, StdError> getRes(String s) {
        return new Result.Ok<>(new Test(s));
    }

    public static Result.Err<Test, StdError> getError() {
        return new Result.Err<>(new StdError("no hello for you", null));
    }

}