<!-- TEMPLATE for not found error page
  * Description: Displays a 404 error if a not available route is adressed
 -->
<template>
    <v-container fluid fill-height>
    <v-layout  justify-center align-center> 
            <span class="text-md-center">
            <h1>404 Error</h1>

            <p>The resource could not be found.</p>
            
            <v-btn :to="{path:'/overview'}">Back</v-btn>

            </span>
            </v-layout>
            </v-container>
</template>


