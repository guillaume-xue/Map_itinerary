/**
 * 
 */
package fr.u_paris.gla.project.utils;

import java.util.Objects;


public class Pair<K, V> {
    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setKey(K key){
        this.key = key;
    }
    
    public void setValue(V value){
        this.value = value; 
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        Pair<?, ?> other = (Pair<?, ?>) o;
        return Objects.equals(key, other.getKey()) && Objects.equals(value, other.getValue());
    }

}