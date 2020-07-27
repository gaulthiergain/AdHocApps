# TestingFramework

This application is a testing framework for the _adhoclib_ [library](https://github.com/gaulthiergain/AdHocLib). 

It provides integrations and unist tests to the library components.
  
### Installation and documentation

For library installation and documentation, a wiki is available on this [address](https://github.com/gaulthiergain/AdHocLib/wiki).

### Intgrations Tests

Bluetooth Tests:
* Try to scan when bluetooth is disabled -> Behaviour: DeviceException (Bt is disabled)
* Disable bluetooth during discovery -> Behaviour: DeviceException (Unable to complete the discovery due to bluetooth connectivity)
* Connect (with 1 attempt) to scanned device which became offline -> Behaviour: NoConnectionException after 1 attempt (Unable to connect to ...)
* Connect (with 3 attempts) to scanned device which became offline -> Behaviour: NoConnectionException after 3 attempts (Unable to connect to ...)
* Try to connect to a device which has bluetooth enabled but which is not listening -> Behaviour: NoConnectionException (Unable to connect to ...)
* Try to connect to a device with bluetooth disabled -> Behaviour: DeviceException (No bluetooth connectivity)
* Multiple connections to a device which has only one listening thread -> Behaviour: only one connection is accepted (NoConnectionException on refused device after timeout)
* Connect to other device(s) -> Behaviour: connection is/are performed
* Send data to other device(s) -> Behaviour: other device(s) received data
* Broadcast data -> Behaviour: other device(s) received data
* Broadcast except(x) data -> Behaviour: other device(s) except host x received data
* Other device(s) send data -> Behaviour: receive data from other device(s)
* Disconnect from other device(s) -> Behaviour: disconnection is/are performed
* Disable Bluetooth during communication -> Behaviour: connection is closed on both sides
* Send a message with bluetooth disabled -> Behaviour: DeviceException (Bt is disabled)
* Broadcast message with bluetooth disabled -> Behaviour: DeviceException (Bt is disabled)
* Broadcast (except particulary host) message with bluetooth disabled -> Behaviour: DeviceException (Bt is disabled)
* Connect to a device already connected -> Behaviour: DeviceAlreadyConnectedException (is alredy connected)
* Discovery during communication -> Behaviour: connection is not closed and discovery is performed
* Discovery during sending messages -> Behaviour: messages are sent and discovery is performed
* Discovery during receiving messages -> Behaviour: messages are received and discovery is performed
* Disconnect and reconnect again -> Behaviour: connection success, disconnect success and reconnection success
* Send and received messages in json -> Behaviour: serialize and deserialize ok
* Send and received messages in bytes -> Behaviour: serialize and deserialize ok
* Connection with flooding event -> Behaviour: must receive distant connection node
* Test status of broadcast/broadcast -> Behaviour: true if message is broadcasted to pairs otherwise false

Wifi Tests (TCP):
* Try to scan when wifi is disabled -> Behaviour: DeviceException (Wifi is disabled)
* Disable wifi during discovery -> Behaviour: DeviceException (Unable to complete the discovery due to wifi connectivity)
* Connect (with 1 attempt) to scanned device which became offline -> Behaviour: NoConnectionException after 1 attempt (Unable to connect to ...)
* Connect (with 3 attempts) to scanned device which became offline -> Behaviour: NoConnectionException after 3 attempts (Unable to connect to ...)
* Try to connect to a device which has wifi enabled but which is not listening -> Behaviour: NoConnectionException (Unable to connect to ...)
* Try to connect to a device with wifi disabled -> Behaviour: DeviceException (No wifi connectivity)
* Multiple connections to a device which has only one listening thread -> Behaviour: only one connection is accepted (NoConnectionException on refused device after timeout)
* Connect to other device(s) -> Behaviour: connection is/are performed
* Send data to other device(s) -> Behaviour: other device(s) received data
* Broadcast data -> Behaviour: other device(s) received data
* Broadcast except(x) data -> Behaviour: other device(s) except host x received data
* Disconnect from other device(s) -> Behaviour: disconnection is/are performed
* Disable wifi during communication -> Behaviour: connection is closed on both sides
* Send a message with bluetooth disabled -> Behaviour: DeviceException (wifi is disabled)
* Broadcast message with bluetooth disabled -> Behaviour: DeviceException (wifi is disabled)
* Broadcast (except particulary host) message with bluetooth disabled -> Behaviour: DeviceException (wifi is disabled)
* Check if the device is a group Owner -> Behaviour: return true is the device is currently the group owner otherwise false
* Set the groupOwner intent value to max (15) -> Behaviour: device must be the group owner
* Test status of broadcast/broadcast -> Behaviour: true if message is broadcasted to pairs otherwise false

Wifi Tests (UDP):
* Try to scan when wifi is disabled -> Behaviour: DeviceException (Wifi is disabled)
* Disable wifi during discovery -> Behaviour: DeviceException (Unable to complete the discovery due to wifi connectivity)
* Connect (with 1 attempt) to scanned device which became offline -> Behaviour: NoConnectionException after 1 attempt (Unable to connect to ...)
* Connect (with 3 attempts) to scanned device which became offline -> Behaviour: NoConnectionException after 3 attempts (Unable to connect to ...)
* Try to connect to a device which has wifi enabled but which is not listening -> Behaviour: NoConnectionException (Unable to connect to ...)
* Try to connect to a device with wifi disabled -> Behaviour: DeviceException (No wifi connectivity)
* Send data to other device(s) -> Behaviour: other device(s) received data
* Broadcast data -> Behaviour: other device(s) received data
* Broadcast except(x) data -> Behaviour: other device(s) except host x received data
* Disconnect from other device(s) -> Behaviour: disconnection is/are performed
* Disable wifi during communication -> Behaviour: connection is closed on both sides
* Send a message with bluetooth disabled -> Behaviour: DeviceException (wifi is disabled)
* Broadcast message with bluetooth disabled -> Behaviour: DeviceException (wifi is disabled)
* Broadcast (except particulary host) message with bluetooth disabled -> Behaviour: DeviceException (wifi is disabled)
* Check if the device is a group Owner -> Behaviour: return true is the device is currently the group owner otherwise false	
* Set the groupOwner intent value to max (15) -> Behaviour: device must be the group owner
* Test status of broadcast/broadcast -> Behaviour: true if message is broadcasted to pairs otherwise false
