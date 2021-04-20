package com.example.demo.web;

import com.example.demo.model.Book;
import com.example.demo.model.Error;
import com.example.demo.model.Info;
import com.example.demo.model.State;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.print.DocFlavor;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class BookController {
    List<Book> books;
    public BookController(){

        books=new ArrayList<Book>();
        readBooksFromCSV();
    }
    private void readBooksFromCSV()
    {
        Path pathToFile = Paths.get("books.csv");

        try (BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.UTF_8))
        {
            String line = br.readLine();
            line = br.readLine();
            while (line != null) {
                String[] attributes = line.split(";");
                addBook(attributes);
                line = br.readLine();
            }
        }catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void addBook(String[] attributes){
        Book tmpBook = new Book(attributes[0],attributes[1],attributes[2],attributes[3],attributes[4],Integer.parseInt(attributes[5]));
        books.add(tmpBook);
    }

    private boolean validBookData(Book bookToValidate){
        if(bookToValidate.getTitle().isEmpty() || bookToValidate.getTitle().isBlank()){
            return false;
        }
        if(bookToValidate.getAuthor().isEmpty() || bookToValidate.getAuthor().isBlank()){
            return false;
        }
        if(bookToValidate.getPublisher().isEmpty() || bookToValidate.getPublisher().isBlank()){
            return false;
        }
        if(bookToValidate.getGenre().isEmpty() || bookToValidate.getGenre().isBlank()){
            return false;
        }
        if(bookToValidate.getYop()<0){
            return false;
        }
        return true;
    }

    private boolean validID(String id)
    {
        try{
            UUID.fromString(id);
            return true;
        }catch (IllegalArgumentException exception)
        {
            return false;
        }

    }

    @GetMapping(path = "/bookshop/book")
    public List<Book> getBook(@RequestParam(value = "publisher",required = false) String publisher,
                              @RequestParam(value = "genre",required = false) String genre){
        List<Book> tmpBooks=books;
        if(publisher != null && !publisher.isEmpty())
        {
            tmpBooks = tmpBooks.stream().filter(pub-> pub.getPublisher().equals(publisher)).collect(Collectors.toList());
        }

        if(genre != null && !genre.isEmpty())
        {
            tmpBooks = tmpBooks.stream().filter(g-> g.getGenre().equals(genre)).collect(Collectors.toList());
        }
        return tmpBooks;
    }

    @GetMapping(path = "/bookshop/verify")
    public Object veryfyAvailability(@RequestParam(value = "id",required = true) String id){
        JSONObject obj = new JSONObject(callGet(id));
        if(obj.opt("status")==null)
        {
            return getNewBookFromJSONObject(obj,id);
        }
        else
        {
            if(obj.getString("status").equals("book.id.not.correct"))
            {
                return new Error("Book id isn't correct",obj.getString("time"));
            }
            else if(obj.getString("status").equals("book.not.found"))
            {
                return new Error("Book not found",obj.getString("time"));
            }
            else{
                return new Error("Undentified error",new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
            }
        }

    }

    private Book getNewBookFromJSONObject(JSONObject obj, String id){
        return new Book(id,obj.getString("tt"),obj.getString("at"),obj.getString("pl"),obj.getString("gn"),obj.getInt("pub"));
    }

    private String callGet(String id){
        try
        {
            RestTemplate restTemplate = new RestTemplate();
            String url = "http://vps749415.ovh.net/store/book/"+id;
            ResponseEntity<String> exchange = restTemplate.exchange(url,HttpMethod.GET,
                    HttpEntity.EMPTY, String.class);
            return exchange.getBody();
        }catch(HttpServerErrorException.InternalServerError e)
        {
            return e.getResponseBodyAsString();
        }

    }

    @PostMapping(value = "/bookshop/book")
    public State addBook(@Validated @RequestBody(required = true) Book newBook){
        State state;// =
        boolean nowOnBookShelf = containsInBookShop(newBook);
        if(nowOnBookShelf)
        {
            state = new Error("Book already existing in bookshop",new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        }
        else{
            newBook.setId(UUID.randomUUID().toString());
            books.add(newBook);
            state = new Info("Book has been added",new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        }

        return state;
    }

    private boolean containsInBookShop(Book newBook)
    {
        for (Book book:
             books) {
            if(book.equals(newBook)) return true;
        }
        return false;
    }

    @PutMapping(path = "/bookshop/book")
    public State updateBook(@RequestParam(value = "id",required = true) String id, @Validated @RequestBody(required = true) Book updatedBook){
        State state;
        Book bookToUpdate = books.stream().filter(x->x.getId().equals(id)).findAny().orElse(null);
        if(bookToUpdate==null){
            state = new Error("Book with this id not found",new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        }
        else{
            books.remove(bookToUpdate);
            updatedBook.setId(id);
            books.add(updatedBook);
            state = new Info("Book has been updated",new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        }

        return state;
    }
    @DeleteMapping(path = "/bookshop/book")
    public State deleteBook(@RequestParam(value = "id",required = true) String id){
        State state;
        Book bookToDelete = books.stream().filter(x->x.getId().equals(id)).findAny().orElse(null);
        if(bookToDelete==null){
            state = new Error("Book whit this id not found",new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        }
        else
        {
            books.remove(bookToDelete);
            state = new Info("Book has been removed",new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        }

        return state;
    }
}
