# Bluetooth LE app experiment

## Goal
I created this app to see how hard it is to use Bluetooth Low Energy for proximity detection. The app scans for
other devices and it advertises itself as a device.

## Functions
#### ID
The app generates an ID that it uses to advertise via Bluetooth LE. You can change the ID to mimic a different device.

#### Discovering
You can set discovering on which means the app starts discovering (scanning) for Bluetooth LE devices that 
advertise the right UUID (built into the app, this is not the id mentioned above). The Bluetooth UUID defines
 a service, like heartrate, headphone or battery.
 
 #### Peripheral
 You switch this on, the app behaves like a Bluetooth peripheral (similar to a headphone, etc). It advertises
 its unique ID, which you can change in the main screen.
 
 #### Advertising mode
 This is the strategy bluetooth LE should use for advertising the device. Choices are
 - low power: saving energy
 - balanced: optimal balance 
 - low latency: best performance, high power consumption

 #### Signal strength
 The signal strength for advertising our device. This can be 
 - ultra low
 - low
 - medium
 - high
 
 #### Clear list
 Clears the list of contacts in your screen. It doesn't affect the database.
 
 #### New ID
 You can change the app unique ID.
 
 #### Contacts
 The app stores all IDs of devices it has found while scanning. The ID's appear in the main screen
 as they come up, they are stored in a database that you can browse in the Contacts screen (swipe for that screen).
 For each contact is shown
 - ID
 - date and time
 - number of detections
 - signal strength set by sender
 - the actual strength
 One could use a combination of signal strength and number of detections to determine whether a contact
 needs to be registered as a "detected contact". Number of detections is done within a limited time frame, 15 minutes.
 
 The clear button on the right clears the database and the contact screen. 
 

