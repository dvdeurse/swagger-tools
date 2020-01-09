package org.swaggertools.demo.web;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.swaggertools.demo.model.Pet;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class PetsControllerImpl implements PetsApi {
    Long counter = 0L;
    Map<Long, Pet> pets = new HashMap<>();

    @Override
    public List<Pet> listPets(Integer limit) {
        Stream<Pet> stream = pets.values().stream();
        if (limit != null) {
            stream = stream.limit(limit);
        }
        return stream.collect(Collectors.toList());
    }

    @Override
    public Pet createPet(Pet requestBody) {
        requestBody.setId(++counter);
        pets.put(requestBody.getId(), requestBody);
        return requestBody;
    }

    @Override
    public Pet getPetById(Long petId, Boolean details) {
        return getPet(petId);
    }

    @Override
    public void updatePet(Long petId, Pet requestBody) {
        pets.put(petId, requestBody);
    }

    @Override
    public void deletePetById(Long petId) {
        getPet(petId);
        pets.remove(petId);
    }

    @Override
    public SseEmitter getPetEvents(Long petId) {
        SseEmitter emitter = new SseEmitter();
        ExecutorService sseMvcExecutor = Executors.newSingleThreadExecutor();
        sseMvcExecutor.execute(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    SseEmitter.SseEventBuilder event = SseEmitter.event()
                            .data("SSE MVC - " + LocalTime.now().toString())
                            .id(String.valueOf(i))
                            .name("sse event - mvc");
                    emitter.send(event);
                    Thread.sleep(100);
                }
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }

    private Pet getPet(Long petId) {
        Pet pet = pets.get(petId);
        if (pet == null) {
            throw new IllegalArgumentException("Pet not found: " + petId);
        }
        return pet;
    }

}