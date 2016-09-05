# WikipediaWords

Frequencies of words in wikipedia


### Running

 > javac -cp .:jsoup-1.9.2.jar WikipediaWords.java

 > java -cp .:jsoup-1.9.2.jar WikipediaWords [run-time-seconds] [num-threads]

My computer optimizes at around 100 threads (note this is Java threads and not real threads), hitting around 106 articles per second.