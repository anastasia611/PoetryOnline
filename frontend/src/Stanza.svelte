<script>
    import Line from "./Line.svelte";
    import {push, remove} from "./common/arrays";

    export let lines = [];

    const REMOVE_DELAY = 150;
    const ADD_DELAY = 150;

    const removeLine = i => {
        setTimeout(() => {
            remove(lines, i);
            lines = lines;
        }, REMOVE_DELAY);
    };

    const addLine = i => {
        setTimeout(() => {
            console.log(push(lines, i, []))
            lines = push(lines, i, []);
        }, ADD_DELAY);
    };
</script>

<div class="stanza">
    {#each lines as words, i}
        <Line {words}
              on:removeLine={() => removeLine(i)}
              on:addLine={() => addLine(i)}/>
    {/each}
</div>

<style lang="scss">
    .stanza {
        margin-bottom: 2rem;
    }
</style>