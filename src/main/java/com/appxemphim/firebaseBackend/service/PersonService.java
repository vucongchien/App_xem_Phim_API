package com.appxemphim.firebaseBackend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.appxemphim.firebaseBackend.dto.request.PersonRequest;
import com.appxemphim.firebaseBackend.model.MovieGenres;
import com.appxemphim.firebaseBackend.model.Person;
import com.appxemphim.firebaseBackend.model.PersonMoive;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;



@Service
public class PersonService {
    
    private final Firestore db = FirestoreClient.getFirestore();
    
    public List<Person> findALLActorForMovie(String movie_id,String collection_name){
        List<Person> persons = new ArrayList<>();
        try {
            CollectionReference movieActorRef = db.collection(collection_name);
            Query query = movieActorRef.whereEqualTo("movive_Id", movie_id);
            ApiFuture<QuerySnapshot> querySnapshot = query.get();

            for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
                String actorDirectorId = document.getString("actor_Director_id");
                if (actorDirectorId != null && !actorDirectorId.isEmpty()) {
                    DocumentReference actorRef = db.collection("Actor_Direction").document(actorDirectorId);
                    ApiFuture<DocumentSnapshot> future = actorRef.get();
                    DocumentSnapshot actorDoc = future.get();

                    if (actorDoc.exists()) {
                        Person person = actorDoc.toObject(Person.class);
                        persons.add(person);
                    }
                }
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e.getMessage());
        }
        return persons;
    }

    public String create(PersonRequest personRequest){
        try{
            Person person = new Person();
            DocumentReference personRef = db.collection("Actor_Direction").document();
            person.setActor_Director_id(personRef.getId());
            person.setName(personRequest.getName());
            person.setNation(personRequest.getNation());
            ApiFuture<com.google.cloud.firestore.WriteResult> future = personRef.set(person);
            future.get();
            return "Thêm tác giả - diễn viên thành công";
        }catch( Exception e){
            throw new RuntimeException("Lỗi khi thêm tác giả - diễn viên: " + e.getMessage());
        }
    }

     public String addmoviePerson(String loai,PersonMoive personMoive){
        try{
            PersonMoive mg = new PersonMoive();
             DocumentReference docRef = null;
            if (loai.equals("actor")) {
                docRef = db.collection("Movie_Actor").document();
            } else if (loai.equals("director")) {
                docRef = db.collection("Movie_Director").document();
            } else {
                throw new IllegalArgumentException("Loại không hợp lệ: " + loai);
            }
            mg.setActor_Director_id(personMoive.getActor_Director_id());
            mg.setMovive_Id(personMoive.getMovive_Id());
            docRef.set(mg).get();
            return "Thêm person cho phim thành công!";
        }catch (Exception e){
        throw new RuntimeException("Lỗi khi thêm movieGenres: "+ e.getMessage());
        }
    }
    
}
