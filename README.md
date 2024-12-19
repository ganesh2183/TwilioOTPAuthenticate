# TwilioOTPAuthenticate
This extension utilizes session handling rules to provide a Twilio OTP code to outgoing requests, it works in **BurpSuite Pro**

<div style='margin-top: 10px; font-size: 12px;'>
                        <h3 style='text-align: left; font-size: 14px; color: #000;'>How to Configure:</h3>
                         <ol>
                              <li>Load the extension into <b>Extensions > Installed > Add > Extension Type: Java > Choose the jar file</b></li>
                           <li> Go to <b>Twilio OTP Authenticate</b> UI interface - Save <b>Twilio Settings</b> tab with Account SID, Auth Token, From Number, To Number. In <b>Configure</b> tab set Rule Type, Parameter Name and click <b>Generate Config</b> button.</li>
                              <li>Go to <b>Settings > Search > Sessions</b></li>
                              <li>Under <b>Session handling rules</b>, go to <b>Add > Rule actions > Add > Invoke a Burp extension</b>,<br>
                                  select '<b>Twilio OTP Authenticate</b>' from the dropdown list available and click OK.</li>
                              <li>Click across to the <b>Scope</b> tab, ensuring that the <b>Tools scope > Scanner, Repeater</b> box is checked.</li>                            
                              <li>Configure the URL scope appropriately. Click OK.</li>
                           <li>Go to <b>Extensions</b> > <b>Installed</b> and reload the extension (uncheck the Twilio OTP Authenticate "Loaded" checkbox, and click it again)</li>
                              <li>Now you can perform security testing in Burp Suite Professional.</li>
                         </ol>
                     </div>


<img width="665" alt="image" src="https://github.com/user-attachments/assets/38a03cb1-835b-4a4e-9281-9838380c1fae" />

<img width="665" alt="image" src="https://github.com/user-attachments/assets/bc93d6bc-5fc3-4e51-be5b-005f9f37f458" />

<img width="665" alt="image" src="https://github.com/user-attachments/assets/f918d43c-21f8-4f2f-b10c-152176095513" />

<img width="665" alt="image" src="https://github.com/user-attachments/assets/839a3024-a230-4b84-89d3-fc3d21486cb5" />

<div style='margin-top: 10px; font-size: 12px;'>
<h3 style='text-align: left; font-size: 14px; color: #000;'>How to build jar file using Gradle:</h3>
<ol>
  <li>Clone the repo.</li>
  <li>Install latest version of Gradle, follow the installation instructions <a href="https://gradle.org/install/"> here</a>.</li>
  <li>Once Gradle is installed, run <b>gradle fatJar</b> from the installation directory using the command line.</li>
  <li>Jar file is generated under(../build/libs/TwilioOTP-1.0-SNAPSHOT.jar) </li>
</ol>
</div>




