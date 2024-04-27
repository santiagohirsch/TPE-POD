package ar.edu.itba.pod.grpc.server.utils;

public class Pair<T, K> {
    private T left;
    private K right;

    public Pair(T left, K right) {
        this.left = left;
        this.right = right;
    }

    public T getLeft() {
        return left;
    }

    public void setLeft(T left) {
        this.left = left;
    }

    public K getRight() {
        return right;
    }

    public void setRight(K right) {
        this.right = right;
    }


}
