<script>
    import RemoveIcon from "./RemoveButton.svelte";
    import {createEventDispatcher} from 'svelte';

    export let word = "";

    const title = 'Удалить слово';
    let invisible = true;

    const dispatch = createEventDispatcher();

    const onRemove = () => {
        dispatch('removeWord');
    };
</script>

<div class="word"
     on:mouseover={() => invisible = false}
     on:mouseleave={() => invisible = true}>

    <span class="word-text" title={word}>
        {word}
    </span>

    <span class:invisible class="remove">
        <RemoveIcon size="10" {title} on:click={onRemove}/>
    </span>
</div>

<style lang="scss">
    .word {
        display: inline-block;
        width: fit-content;
        padding: 0.5rem;
        line-height: 1rem;
        color: #222;
        cursor: default;
        border-radius: 0.125rem;
        background-clip: padding-box;
        user-select: none;
        /*background-color: #F0CBCB;*/

        &:hover, &:focus {
            background-color: #dddddd;
        }
    }

    .word-text {
        color: #500808;
    }

    .remove {
        &.invisible {
            opacity: 0;
        }
    }
</style>