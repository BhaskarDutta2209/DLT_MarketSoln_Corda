# DLT_MarketSoln_Corda


<h3> Set up </h3>

<ul>
<li>Clone this git repo</li>
<li>Move into the project folder</li>
<li>Compile the code to create the jar files for all the nodes by <code>./gradlew clean deployNodes</code></li>
<li>The jar file for each node is individually available in it's respective folder in <code>./build/nodes/</code></li>
<li>To run all the nodes at the same time, in Linux/Mac type <code>./build/nodes/runnodes</code></li>
</ul>

<h3>Initial Steps</h3>
<p>After each node is up and running follow this initial steps</p>
<ul>
<li>In the <strong>Delivery</strong> node type <code>flow start CreateNewAccount accountName: Delivery, shareWith: [Bank,Shop,Buyer]</code></li>
<li>In the <strong>Bank</strong> node type <code>flow start CreateNewAccount accountName: Bank, shareWith: [Buyer,Shop,Delivery]</code></li>
</ul>

<h3>Starting the server</h3>
<p>The framework have integrated <strong>Spring Boot Framework</strong>. To start the server type<code>./gradlew runTemplateServer</code></p>