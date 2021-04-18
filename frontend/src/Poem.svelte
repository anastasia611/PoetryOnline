<script>
    import { createEventDispatcher } from 'svelte';
    import Stanza from "./Stanza.svelte";

    export let stanzas = [];
    export let stanzaIndex = 0;
    export let lineIndex = 0;
    export let wordIndex = 0;

    const dispatch = createEventDispatcher();

    const onNext = () => {
        if (stanzaIndex < stanzas.length - 1) {
            stanzaIndex++;
            lineIndex = 0;
            wordIndex = 0;
            console.log('nxt')
        }
    };

    const onBack = () => {
        if (stanzaIndex > 0) {
            const stanza = stanzas[--stanzaIndex];
            lineIndex = stanza.length - 1;
            const line = stanza[lineIndex];
            wordIndex = line.length - 1;
            console.log('bc', stanzaIndex, lineIndex, wordIndex)
        }
    };

    const onFocus = i => {
        stanzaIndex = i;
        dispatch('focus');
    };
</script>

<div class="poem">
    <h2 contenteditable>Стихотворение</h2>
    {#each stanzas as lines, i}
        <Stanza
                {lines}
                wordIndex={i === stanzaIndex ? wordIndex : -1}
                lineIndex={i === stanzaIndex ? lineIndex : -1}
                on:focus={() => onFocus(i)}
                on:back={onBack}
                on:next={onNext} />
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