package com.rtlabs.common.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

public class RtCollectionsUtil {
	public static <T, K, V, M extends Map<K, V>> Collector<T, ?, M> toMap(Function<T, K> keyMapper, Function<T, V> valueMapper, Supplier<M> mapSupplier) {
		return Collector.of(
			mapSupplier, 
			(M m, T e) -> m.put(keyMapper.apply(e), valueMapper.apply(e)), 
			(M m1, M m2) -> { m1.putAll(m2); return m1; });
	}

	public static <T, K, V, M extends Multimap<K, V>> Collector<T, ?, M> toMultimap(
			Function<? super T, ? extends K> keyMapper, 
			Function<? super T, ? extends V> valueMapper, 
			Supplier<M> mapSupplier) {
		
		return Collector.of(
			mapSupplier, 
			(M m, T e) -> m.put(keyMapper.apply(e), valueMapper.apply(e)), 
			(M m1, M m2) -> { m1.putAll(m2); return m1; });
	}

	public static <T, K, V> Collector<T, ?, LinkedListMultimap<K, V>> toMultimap(
			Function<? super T, ? extends K> keyMapper, 
			Function<? super T, ? extends V> valueMapper) {
		return toMultimap(keyMapper, valueMapper, LinkedListMultimap::create);
	}

	public static <T, K> Collector<T, ?, LinkedListMultimap<K, T>> toMultimap(Function<? super T, ? extends K> keyMapper) {
		return toMultimap(keyMapper, Function.identity(), LinkedListMultimap::create);
	}

	public static <K, V> Collector<Entry<K, V>, ?, LinkedListMultimap<K, V>> toMultimap() {
		return toMultimap(Entry::getKey, Entry::getValue, LinkedListMultimap::create);
	}

	
	public static <K, V> Collector<Entry<K, V>, ?, ImmutableMap<K, V>> toImmutableMap() {
		return toImmutableMap(Entry::getKey, Entry::getValue);
	}

	public static <T, K> Collector<T, ?, ImmutableMap<K, T>> toImmutableMap(Function<? super T, ? extends K> keyMapper) {
		return toImmutableMap(keyMapper, o -> o);
	}
	
	public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
			Function<? super T, ? extends K> keyMapper,
			Function<? super T, ? extends V> valueMapper) {
		return Collector.<T, ImmutableMap.Builder<K, V>, ImmutableMap<K, V>>of(
			ImmutableMap::builder, 
			(m, e) -> m.put(keyMapper.apply(e), valueMapper.apply(e)), 
			(m1, m2) ->m1.putAll(m2.build()),
			ImmutableMap.Builder::build);
	}
}
