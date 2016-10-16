## Programming task

### Implement console based client and server with following specifications:

#### Client:
1. Captures sound from default microphone (in real time and continouously)
2. Every k seconds it sends n seconds long fragment of audio to server. Data packet should have following format:
  * Magic number (2 bytes) 0x12 0x34
  * Packet size (2 bytes) (sizeof(timestamp) + sizeof(sound format) + samples.length)
  * Timestamp of first recordSample in payload (8 bytes)
  * Sound format (2 bytes) (encode it in any way) 
  * Sound samples
3. By default k = 2 and n = 4 i.e. every 2 seconds client sends 4 seconds of audio (first packet is sent after 4 seconds since the start of recording)

#### Server:
1. receives the packets from clients and saves sound samples payload to files. File system structure should encode the metadata (timestamp and sound format). File should contain only the sound samples/ i.e. /home/me/S16LE/1476188202000.pcm
2. Responds with a packet in following format:
  * magic number (2 bytes) 0x12 0x34
  * Timestamp (echo from client) (8 bytes) 

**Note**: Simplicity of code and readability is the most important. Keep in mind that some of the parts of code could change in the future (more audio inputs, different packet format).