<!-- TEMPLATE for no permission error page
  * Description: Displays a 401 error "permission denied" if an unauthorised user try to access a login protected ressource
 -->
<template>
    <v-container fluid fill-height>
    <v-layout  justify-center align-center> 
            <span class="text-md-center">
            <h1>401 Error</h1>

            <p>Permission denied.</p>
            
            <v-btn :to="{path:'/overview'}">Back</v-btn>

            </span>
            </v-layout>
            </v-container>
</template>
