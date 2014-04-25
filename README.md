NivaSmppReceiver
================

A robust SMPP receiver to Beanstalk message queue


## Beanstalk Job Specification
Each new SMPP message should be parsed from the PDU response, an array should be built with the specified parameters according to the message type and then converted into a valid JSON string to be inputed as a new Job to the beanstalk message queue in the corresponding tube.

##### DR message (Delivery Report, MT)

```
{
	"recipient": "55055",
	"id": "10483923",
	"status": "0",
	"dlr": "13"
}
```

Parameters

- `recipient`: the intended recipient short code.
- `id`: the unique trace identifier for the delivery report. it should be encoded as a decimal and **not** as a hex string.
- `status`: delivery status: 0 failure, 1 success.
- `dlr`: delivery report code.

For the `status` and `dlr` codes check the attached DLR specification.

#### MO message (Mobile Originated)

```
{
	"sender": "5512345678",
	"recipient": "55055",
	"content": "This is some sample text message"
}
```

Parameters

- `sender`: the original 10-digit mobile number of the sender.
- `recipient`: the intended recipient short code.
- `content`: the text message content.

## Configuration

Each receiver instance must be configured from a independent configuration file, for example `config.yml` and should include the following configuration parameters:

```
smsc_host: 100.50.20.10 # SMSC host
smsc_port: 5000 # SMSC port
esme_username: esmeusername # ESME username
esme_password: mysecretpassword # ESME password
esme_system_type: www # ESME System Type
carrier: I # Affects TON & NPI settings. I: Iusacell, M: Movistar
shortcode: 55055
type: (MO/MT) # MTs convert the messages to the DLR spec while MO use the MO spec
beanstalk_host: localhost # Beanstalk server host
beanstalk_port: 11300 # Beanstalk server port
beanstalk_tube: mo # Beanstalk tube to put
```

#### Carrier Settings

```
Iusacell MX
Source TON: 0x01 (TON_INTERNATIONAL)
Source NPI: 0x01 (NPI_E164)
Destination TON: 0x01 (TON_INTERNATIONAL)
Destination NPI: 0x01 (NPI_E164)

Movistar MX
Source TON: 0x04 (TON_SUBSCRIBERNUMBER)
Source NPI: 0x09 (NPI_PRIVATE)
Destination TON: 0x02 (TON_NATIONAL)
Destination NPI: 0x01 (NPI_E164)
```


## Dependencies

Libraries are not included in the src. Here is the list of needed libraries:

```
BeanstalkClient-1.4.6.jar
ch-commons-charset-3.0.0.jar
ch-commons-gsm-3.0.0.jar
ch-commons-util-6.0.1.jar
ch-smpp-5.0.5.jar
gson-2.2.4.jar
junit-4.11.jar
logback-classic-1.1.1.jar
logback-core-1.1.1.jar
netty-3.9.0.Final.jar
slf4j-api-1.7.6.jar
snakeyaml-1.13.jar
joda-time-2.3.jar
```

They can be found in Maven Repositories
