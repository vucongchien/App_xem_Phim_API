package com.appxemphim.firebaseBackend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.appxemphim.firebaseBackend.dto.request.PersonRequest;
import com.appxemphim.firebaseBackend.model.MovieGenres;
import com.appxemphim.firebaseBackend.model.Person;
import com.appxemphim.firebaseBackend.model.PersonMoive;
import com.appxemphim.firebaseBackend.service.PersonService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/person")
@RequiredArgsConstructor
public class PersonController {
    
    private final PersonService personService;
                        
    @GetMapping("/actor/{movie_id}")
    public List<Person> findAllActorForMovie(@PathVariable String movie_id) {
        return personService.findALLActorForMovie(movie_id,"Movie_Actor");
    }

    @GetMapping("/director/{movie_id}")
    public List<Person> findAllDirectorForMovie(@PathVariable String movie_id) {
        return personService.findALLActorForMovie(movie_id,"Movie_Director");
    }

    @PostMapping("/create")
    public String create(@RequestBody PersonRequest personRequest) {
        return personService.create(personRequest);
    }
    
    @PostMapping("/personinmovie/{loai}")
    public ResponseEntity<String> personnmovie(@RequestBody PersonMoive personMoive,@PathVariable String loai) {
        try{
        String  result = personService.addmoviePerson(loai,personMoive);
        return ResponseEntity.ok(result);
       }catch(RuntimeException e) {
        return ResponseEntity.status(401).body(e.getMessage());
    }
    }
    
}
