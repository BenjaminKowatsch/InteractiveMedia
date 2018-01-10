<template>

<p>{{ groupCount }} World!</p>

</template>


<script>
import Overview from "../pages/overview.vue";

export default ({
    name: "Datacollector",
    props: ['groupCount'],

    data: function () {

        return {
            greeting: 'Hello'

        }

    },
    mounted: function() {

        console.log("GroupsCount in Datacollecor")
        console.log(this.groupCount)
    }

})

</script>