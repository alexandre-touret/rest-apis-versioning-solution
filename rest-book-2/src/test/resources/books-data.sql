delete from book_authors ;
delete from author ;
delete from book ;
commit;
INSERT INTO author(id,firstname,lastname,public_id) VALUES (1000,'Harriet','Beecher Stowe','7c11e1bf-1c74-4280-812b-cbc6038b7d21');
insert into book (id,title,isbn_13,isbn_10,year_of_publication,nb_of_pages,rank,small_image_url,medium_image_url,description) values (100,'la case de l oncle tom','1234567899123','1234567890',1852,613,4.2,null,null,'Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.');
insert into book_authors(books_id, authors_id) VALUES (100,1000);
