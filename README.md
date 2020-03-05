# Source Code of Goldbrute

A new botnet tracked as GoldBrute has appeared in the threat landscape, it is scanning the web for Windows machines with Remote Desktop Protocol (RDP) connection enabled.

The botnet is currently targeting over 1.5 million unique endpoints online, it is used to brute-force RDP connections or to carry out credential stuffing attacks.

“This botnet is currently brute forcing a list of about 1.5 million RDP servers exposed to the Internet. Shdoan lists about 2.4 million exposed servers  [1]. GoldBrute uses its own list and is extending it as it continues to scan and grow.” wrote the researchers Renato Marinho of Morphus Labs who discovered the bot.

The GoldBrute botnet currently has a single command and control server (104[.]156[.]249[.]231), its bots exchange data with the C2 via AES encrypted WebSocket connections to port 8333. 

Querying the Shodan search engine for systems with RDP enabled it is possible to find roughly 2.4 million machines.

“An infected system will first be instructed to download the bot code. The download is very large (80 MBytes) and includes the complete Java Runtime. The bot itself is implemented in a Java class called GoldBrute” continues the expert.

“Initially, the bot will start scanning random IP addresses to find more hosts with exposed RDP servers. These IPs are reported back to the C&C server. After the bot reported 80 new victims, the C&C server will assign a set of targets to brute force to the bot.” 
GoldBrute botnet

![Image The complete attack chain](https://www.linuxprobe.com/wp-content/uploads/2019/06/2-7.jpg)

Botnet brute-forces RDP connection and gains access to a poorly protected Windows system.
It downloads a big zip archive containing the GoldBrute Java code and the Java runtime itself. It uncompresses and runs a jar file called “bitcoin.dll”.

The bot will start to scan the internet for “brutable” RDP servers and send their IPs to the C2 that in turn sends a list of IP addresses to brute force.

GoldBrute bot gets different “host + username + password”  combinations.
Bot performs brute-force attack and reports result back to C2 server.
According to the researcher, the list of “brutable” RDP targets is rapidly growing, this suggests that also the size of the botnet is increasing.

“Analyzing the GoldBrute code and understanding its parameters and thresholds, it was possible to manipulate the code to make it save all “host + username + password” combinations on our lab machine.” continues the expert.

“After 6 hours, we received 2.1 million IP addresses from the C2 server from which 1,596,571 are unique. Of course, we didn’t execute the brute-force phase. With the help of an ELK stack, it was easy to geolocate and plot all the addresses in a global world map, as shown below.”
goldbrute botnet map

The GoldBrute botnet is difficult to detect because every bot only launches one password-guessing attempt per victim.
