<!-- TEMPLATE for about view
  * Description: Gives details about this web app and the app Debts²
 -->
<template>
  <v-container fluid fill-height>
    <v-layout  justify-center align-center> 
      <span class="text-md-center">        
        <img src="../assets/logo.png"  alt="Debts² logo" class="logo">
        <h1>About Debts²</h1>
        <br>
        <p>This website contains a monitoring dashboard for administrators of the Debts² mobile application.</p>
        <p>Debts² is an android app to manage debts in user groups like dormitory roommates or vacationing friends.</p>
        <p>There are also additional features like uploading a photo or a geo location to any transaction.</p>
        <p>Debts² is a project of the Stuttgart Media University within the lecture Interactive Media,</p>
        <p>released by five students during the Winter term 2017.</p>
        <v-btn :to="{path:'/overview'}">Back</v-btn>
      </span>
    </v-layout>
  </v-container>
</template>