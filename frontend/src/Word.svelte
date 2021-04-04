<script>
    import RemoveIcon from "./RemoveIcon.svelte";
    import { createEventDispatcher } from 'svelte';

    export let word = "";

    let hover = false;

    const dispatch = createEventDispatcher();

    let onRemove = () => {
        dispatch('removeWord');
    };
</script>

<div class="word">
    <span class="word-text" title={word}>{word}</span>
    <span class="remove-container"
         on:click={onRemove}
         on:mouseover={() => hover = true}
         on:mouseleave={() => hover = false}>
        <RemoveIcon title="Удалить слово" size="10" {hover}/>
    </span>
</div>

<style>
    .word {
        display: inline-block;
        width: fit-content;
        padding: 0.35rem 0.5rem;
        margin: 0.5rem 0 0.5rem 0.5rem;
        line-height: 1rem;
        color: #222;
        cursor: default;
        border-radius: 0.125rem;
        background-clip: padding-box;
        user-select: none;
        background-color: #F0CBCB;
    }

    .word-text {
        color: #500808;
    }

    .remove-container {
        margin-left: 0.5rem;
    }
</style>