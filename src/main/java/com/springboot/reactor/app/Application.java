package com.springboot.reactor.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.springboot.reactor.app.model.Comment;
import com.springboot.reactor.app.model.User;
import com.springboot.reactor.app.model.UserComment;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class Application implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		exampleBackPressure();
	}

	public void exampleBackPressure() throws InterruptedException {

		Flux.range(1, 10).log().subscribe(new Subscriber<Integer>() {

			private Subscription s;
			private Integer limit = 5;
			private Integer consumer = 0;

			@Override
			public void onSubscribe(Subscription s) {
				this.s = s;
				s.request(limit);
			}

			@Override
			public void onNext(Integer t) {
				log.info(t.toString());
				consumer++;
				if (consumer == limit) {
					consumer = 0;
					s.request(limit);
				}
			}

			@Override
			public void onError(Throwable t) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onComplete() {
				// TODO Auto-generated method stub

			}
		});
	}

	public void exampleIntervalCreate() throws InterruptedException {

		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				private Integer count = 0;

				@Override
				public void run() {
					emitter.next(++count);

					if (count == 10) {
						timer.cancel();
						emitter.complete();
					}

					if (count == 5) {
						timer.cancel();
						emitter.error(new InterruptedException("Error, se ha detenido el flux en 5"));
					}

				}
			}, 1000, 1000);
		}).subscribe(next -> log.info(next.toString()), error -> log.error(error.getMessage()),
				() -> log.info("Hemos completado!!!"));
	}

	public void exampleIntervalInfinite() throws InterruptedException {

		CountDownLatch latch = new CountDownLatch(1);

		Flux.interval(Duration.ofSeconds(1)).doOnTerminate(latch::countDown).flatMap(i -> {

			if (i >= 5) {
				return Flux.error(new InterruptedException("Solo hasta 5!"));
			} else {
				return Flux.just(i);
			}
		}).map(i -> "Hola " + i).retry(2).subscribe(i -> log.info(i), e -> log.error(e.getMessage()));

		latch.await();

	}

	public void exampleDelayElements() {
		Flux<Integer> range = Flux.range(1, 12).delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()));

		range.blockLast();

	}

	public void exampleInterval() {
		Flux<Integer> range = Flux.range(1, 12);
		Flux<Long> delay = Flux.interval(Duration.ofSeconds(1));

		range.zipWith(delay, (ra, de) -> ra).doOnNext(i -> log.info(i.toString())).blockLast();

	}

	public void exampleZipWithRange() {
		Flux<Integer> range = Flux.range(0, 4);
		Flux.just(1, 2, 3, 4).map(i -> (i * 2))
				.zipWith(range, (one, two) -> String.format("First Flux: %d, Second Flux: %d", one, two))
				.subscribe(text -> log.info(text));

	}

	public void exampleUserCommentZipWithForm2() {

		Mono<User> userMono = Mono.fromCallable(() -> new User("John", "Doe"));
		Mono<Comment> commentMono = Mono.fromCallable(() -> {
			Comment c = new Comment();
			c.addComment("Hola pepe, que tal!!");
			c.addComment("Me voy a la playa!");
			c.addComment("Estoy tomando un curso de spring con reactor");
			return c;
		});

		Mono<UserComment> userComment = userMono.zipWith(commentMono).map(t1 -> {
			User u = t1.getT1();
			Comment c = t1.getT2();
			return new UserComment(u, c);
		});
		userComment.subscribe(uc -> log.info(uc.toString()));

	}

	public void exampleUserCommentZipWith() {

		Mono<User> userMono = Mono.fromCallable(() -> new User("John", "Doe"));
		Mono<Comment> commentMono = Mono.fromCallable(() -> {
			Comment c = new Comment();
			c.addComment("Hola pepe, que tal!!");
			c.addComment("Me voy a la playa!");
			c.addComment("Estoy tomando un curso de spring con reactor");
			return c;
		});

		Mono<UserComment> userComment = userMono.zipWith(commentMono, (u, c) -> new UserComment(u, c));
		userComment.subscribe(uc -> log.info(uc.toString()));

	}

	public void exampleUserCommentFlatMap() {

		Mono<User> userMono = Mono.fromCallable(() -> new User("John", "Doe"));
		Mono<Comment> commentMono = Mono.fromCallable(() -> {
			Comment c = new Comment();
			c.addComment("Hola pepe, que tal!!");
			c.addComment("Me voy a la playa!");
			c.addComment("Estoy tomando un curso de spring con reactor");
			return c;
		});

		userMono.flatMap(u -> commentMono.map(c -> new UserComment(u, c))).subscribe(uc -> log.info(uc.toString()));

	}

	public void exampleCollectList() throws Exception {

		Flux.fromIterable(returnNameUsingConstructorList()).collectList().subscribe(list -> {
			list.forEach(item -> log.info(item.toString()));
		});

	}

	public void exampleToString() throws Exception {

		Flux.fromIterable(returnNameUsingConstructorList())
				.map(user -> user.getName().toUpperCase().concat(" ").concat(user.getSubname().toUpperCase()))
				.flatMap(name -> {
					if (name.contains("bruce".toUpperCase())) {
						return Mono.just(name);
					} else {
						return Mono.empty();
					}
				}).map(name -> {
					return name.toLowerCase();
				})

				.subscribe(name -> log.info(name));
	}

	public void exampleFlatMap() throws Exception {

		Flux.fromIterable(returnNameList())
				.map(name -> new User(name.split(" ")[0].toUpperCase(), name.split(" ")[1].toUpperCase()))
				.flatMap(user -> {
					if (user.getName().equalsIgnoreCase("bruce")) {
						return Mono.just(user);
					} else {
						return Mono.empty();
					}
				}).map(u -> {
					String name = u.getName().toLowerCase();
					u.setName(name);
					return u;
				})

				.subscribe(e -> log.info(e.getName().concat(" ").concat(e.getSubname())));
	}

	public void exampleIterable() throws Exception {

		Flux<String> names = Flux.fromIterable(returnNameList());

		Flux<User> users = names
				.map(name -> new User(name.split(" ")[0].toUpperCase(), name.split(" ")[1].toUpperCase()))
				.filter(e -> e.getName().toLowerCase().equals("bruce")).doOnNext(e -> {
					if ("".equals(e.getName())) {
						throw new RuntimeException("No existen nombres");
					}
					log.info(e.getName().concat(" ").concat(e.getSubname()));
				})

				.map(u -> {
					String name = u.getName().toLowerCase();
					u.setName(name);
					return u;
				});

		users.subscribe(e -> log.info(e.toString()), error -> log.error(error.getMessage()), new Runnable() {

			@Override
			public void run() {
				log.info("Ha finalizado la ejecución del observable con éxito!");

			}
		});

	}

	private List<String> returnNameList() {
		List<String> usersList = new ArrayList<String>();
		usersList.add("Jhonatan Rojas");
		usersList.add("Charles Terrones");
		usersList.add("Milton Rojas");
		usersList.add("Bruce Lee");
		usersList.add("Bruce Willis");
		return usersList;
	}

	private List<User> returnNameUsingConstructorList() {
		List<User> usersList = new ArrayList<User>();
		usersList.add(new User("Jhonatan", "Rojas"));
		usersList.add(new User("Charles", "Terrones"));
		usersList.add(new User("Milton", "Rojas"));
		usersList.add(new User("Bruce", "Lee"));
		usersList.add(new User("Bruce", "Willis"));
		return usersList;
	}

}