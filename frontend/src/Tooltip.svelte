<script>
    import { createEventDispatcher } from 'svelte';
    import IconButton from "./IconButton.svelte";

    export let open = false;
    export let text;
    export let showConfirm = true;
    export let showReject = true;
    export let up = true;
    export let left = false;
    export let showArrow = true;

    const confirmTitle = 'Готово';
    const rejectTitle = 'Закрыть';

    const dispatch = createEventDispatcher();

    const onConfirm = () => {
        dispatch('confirm');
    };

    const onReject = () => {
        dispatch('reject');
    };

    const onKeyDown = (e) => {
        if (open) {
            if (e.key === 'Escape') {
                console.log('esc')
            } else if (e.key === 'Enter') {
                console.log('ent')
            }
        }
    };

</script>

{#if open}
    <div class="tooltip" class:up={up} class:left={left} on:keydown={onKeyDown}>
        {#if showArrow}
            <div class="tooltip-arrow"></div>
        {/if}
        {text}
        <div class="tooltip-bar">
            {#if showConfirm}
                <div class="tooltip-button left">
                    <IconButton icon="check" height="30" width="30" padding="6"
                                title={confirmTitle} opacity={1} hoverOpacity={0.7}
                                round fill color="white" backColor="#4fd173"
                                on:click={onConfirm}/>
                </div>
            {/if}
            {#if showReject}
                <div class="tooltip-button">
                    <IconButton icon="cross" height="30" width="30" padding="6"
                                title={rejectTitle} opacity={1} hoverOpacity={0.7}
                                round borders
                                on:click={onReject}/>
                </div>
            {/if}
        </div>
    </div>
{/if}

<style lang="scss">
  .tooltip {
    --color: #ffffff;

    position: absolute;
    transform: translate(0, 100%);
    top: 0;
    left: -0.25rem;
    min-width: 15rem;
    background-color: var(--color);
    padding: 0.75rem 0.25rem 0.25rem 0.75rem;
    box-shadow: 0 4px 6px 0 #b7b1ac;
    text-align: left;
    font-size: .8rem;
    z-index: 2;
    box-sizing: border-box;
    border-radius: 0.25rem;

    &.up {
      transform: translateY(-100%);
    }

    &.left {
      transform: translateX(-100%);
    }

    & .tooltip-arrow {
      &::before {
        border-width: 0.6rem 0.45rem 0 0.45rem;
        border-left-color: transparent;
        border-right-color: transparent;
        border-bottom-color: transparent;
        top: calc(100%);
        content: '';
        display: block;
        width: 0;
        height: 0;
        border-style: solid;
        position: absolute;
        color: var(--color);
      }
    }

    & .tooltip-bar {
      display: flex;
      justify-content: center;
      margin-top: 0.75rem;
      margin-bottom: 0.3rem;
    }

    & .tooltip-button {
      &.left {
        margin-right: 1rem;
      }
    }
  }

</style>