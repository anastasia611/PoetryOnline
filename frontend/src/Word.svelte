<script>
    import RemoveButton from "./RemoveButton.svelte";
    import { createEventDispatcher } from 'svelte';
    import { isLetter, isPunctuationMark } from "./common/strings";

    export let word = "";
    export let editable = false;

    const title = 'Удалить слово';

    let invisible = !editable;
    let input;
    $: width = 0;

    const dispatch = createEventDispatcher();

    const onRemove = () => {
        dispatch('removeWord');
    };

    $: if (input) {
        input.addEventListener("keypress", e => {
            onKeyPress(e);
        });
    }

    const onKeyPress = e => {
        if (!isLetter(e.key) && !isPunctuationMark(e.key) && e.key !== ' ') {
            e.preventDefault();
        }

        if (e.key === "Enter") {
            dispatch('enter', { word: input.value });
            invisible = true;
            editable = false;
        } else if (isPunctuationMark(e.key) || e.key === " ") {
            dispatch('punct', { word: input.value, sign: e.key });
            invisible = true;
            editable = false;
        }
    };

    const onFocus = () => {
        editable = true;
    };

    const onBlur = () => {
        if (!word) {
            dispatch('removeWord');
        } else {
            dispatch('editFinished', { word: input.value });
        }

        editable = false;
        invisible = true;
    };

</script>

{#if editable}
    <input
            class="word-editable"
            value={word}
            style="width:{width}px;"
            autofocus
            autocomplete="on"
            autocapitalize="off"
            spellcheck="true"
            placeholder="введите слово.."
            bind:this={input}
            on:blur={onBlur}
    />
{:else}

    <div class="word" tabindex="-1"
         bind:clientWidth={width}
         on:focus={onFocus}
         on:mouseover={() => invisible = false}
         on:mouseleave={() => invisible = true}>

        <span class="word-text" title={word}>
            {word}
        </span>

        <span class:invisible class="remove">
            <RemoveButton size="10" {title} on:click={onRemove}/>
        </span>
    </div>
{/if}

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

        &:hover, &:focus {
            background-color: #dddddd;
            outline: none;
        }
    }

    .word-text {
        color: #500808;
    }

    .word-editable {
        border: none;
        padding: 0 0.5rem;
        height: 2rem;

        &:focus {
            outline: none;
        }
    }

    .remove {
        &.invisible {
            opacity: 0;
        }
    }
</style>