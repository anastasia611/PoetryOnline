<script>
    import Stanza from "./Stanza.svelte";
    export let stanzas = [];
</script>

<div class="poem">
    <h2 contenteditable>Стихотворение</h2>
    {#each stanzas as lines}
        <Stanza {lines} />
    {/each}
</div>

<style lang="scss">
    .poem {
        display: flex;
        width: fit-content;
        flex-direction: column;
        margin-left: auto;
        margin-right: auto;
        padding: 1rem;
    }

    h2 {
        //color: #DE8484;
        margin-left: 0.75rem;
    }
</style>